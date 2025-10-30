package com.agromercado.accounts.cmd.domain.aggregate;

import java.time.Instant;
import java.util.*;

import com.agromercado.accounts.cmd.domain.enum_.EstadoMembresia;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;
import com.agromercado.accounts.cmd.domain.event.usuario.MembresiaZonalOtorgada;
import com.agromercado.accounts.cmd.domain.event.usuario.RolGlobalAgregado;
import com.agromercado.accounts.cmd.domain.event.usuario.UsuarioRegistrado;

public class Usuario {
  private final String usuarioId;
  private String email;
  private String nombre;
  private String estado; // ACTIVO | BLOQUEADO | ELIMINADO
  private int version;

  private final Set<String> rolesGlobales = new HashSet<>();
  private final Map<String, Membresia> membresias = new HashMap<>(); // key = zonaId

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private Usuario(String usuarioId, String email, String nombre) {
    this.usuarioId = usuarioId; this.email = email; this.nombre = nombre;
    this.estado = "ACTIVO"; this.version = 1;
  }

  // NUEVO -> lo usa UsuarioProxy (rehidratación desde JPA)
  protected Usuario(String usuarioId, String email, String nombre, String estado) {
    this.usuarioId = usuarioId;
    this.email = email;
    this.nombre = nombre;
    this.estado = (estado != null ? estado : "ACTIVO");
    this.version = 1;
  }

  // entity interna
  public static class Membresia {
    private final String zonaId;
    private RolZonal rol;
    private EstadoMembresia estado;
    private final Instant createdAt;

    public Membresia(String zonaId, RolZonal rol, EstadoMembresia estado, Instant createdAt) {
      this.zonaId = zonaId; this.rol = rol; this.estado = estado; this.createdAt = createdAt;
    }
    public String zonaId() { return zonaId; }
    public RolZonal rol() { return rol; }
    public EstadoMembresia estado() { return estado; }
    public Instant createdAt() { return createdAt; }
    public void activar() { this.estado = EstadoMembresia.ACTIVA; }
  }

  public static Usuario registrar(String email, String nombre, DomainEvents factory) {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email requerido");
    if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
    String id = "USR-" + UUID.randomUUID();
    var u = new Usuario(id, email, nombre);
    var meta = factory.newMeta("Usuario", id, null, u.version, id, null, null);
    u.domainEvents.add(new UsuarioRegistrado(factory.newEventId(), factory.now(), meta, id, email, nombre));
    return u;
  }

  public void agregarRolGlobal(String rol, DomainEvents factory, String adminActorId) {
    if (rolesGlobales.contains(rol)) return;
    rolesGlobales.add(rol);
    var meta = factory.newMeta("Usuario", usuarioId, null, ++version, adminActorId, null, null);
    domainEvents.add(new RolGlobalAgregado(factory.newEventId(), factory.now(), meta, usuarioId, rol));
  }

  public void otorgarMembresiaZonal(String zonaId, RolZonal rolZonal, DomainEvents factory, String causedBy) {
    var current = membresias.get(zonaId);
    if (current != null && current.estado() == EstadoMembresia.ACTIVA && current.rol() == rolZonal) return; // idempotente
    var now = factory.now();
    var nueva = new Membresia(zonaId, rolZonal, EstadoMembresia.ACTIVA, now);
    membresias.put(zonaId, nueva);
    var meta = factory.newMeta("Usuario", usuarioId, zonaId, ++version, causedBy, null, null);
    domainEvents.add(new MembresiaZonalOtorgada(factory.newEventId(), now, meta, usuarioId, zonaId, rolZonal, EstadoMembresia.ACTIVA));
  }

  public List<DomainEvent> pullDomainEvents() {
    var copy = List.copyOf(domainEvents);
    domainEvents.clear();
    return copy;
  }

  // getters
  public String getUsuarioId() { return usuarioId; }
  public String getEmail() { return email; }
  public String getNombre() { return nombre; }
  public String getEstado() { return estado; }
  public int getVersion() { return version; }
  public Set<String> getRolesGlobales() { return rolesGlobales; }
  public Map<String, Membresia> getMembresias() { return membresias; }
}
