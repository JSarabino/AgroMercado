package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.agromercado.accounts.cmd.domain.aggregate.Usuario;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioMembresiaEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioRolGlobalEntity;

public final class UsuarioMapper {
  private UsuarioMapper() {}

  public static UsuarioEntity toUsuarioEntity(Usuario agg){
    // Usa el ctor público (id, email, nombre, estado, createdAt)
    return new UsuarioEntity(
        agg.getUsuarioId(),
        agg.getEmail(),
        agg.getNombre(),
        agg.getEstado(),
        Instant.now()   // created_at
    );
  }

  public static List<UsuarioRolGlobalEntity> toRolGlobalEntities(Usuario agg){
    return agg.getRolesGlobales().stream()
        // El entity pide (usuarioId, rolGlobal, grantedAt)
        .map(rol -> new UsuarioRolGlobalEntity(agg.getUsuarioId(), rol, Instant.now()))
        .toList();
  }

  public static List<UsuarioMembresiaEntity> toMembresias(Usuario agg){
    return agg.getMembresias().values().stream()
        // El entity pide (usuarioId, zonaId, RolZonal, EstadoMembresia, createdAt)
        .map(m -> new UsuarioMembresiaEntity(
            agg.getUsuarioId(),
            m.zonaId(),
            m.rol(),          // ya es enum RolZonal
            m.estado(),       // ya es enum EstadoMembresia
            m.createdAt()
        ))
        .toList();
  }

  public static Usuario toAggregate(UsuarioEntity base,
                                    Collection<UsuarioRolGlobalEntity> roles,
                                    Collection<UsuarioMembresiaEntity> membresias){
    var u = new UsuarioReflector(base.getUsuarioId(), base.getEmail(), base.getNombre(), base.getEstadoUsuario());
    roles.forEach(r -> u.rolesGlobales.add(r.getRolGlobal()));
    membresias.forEach(m ->
        u.membresias.put(
            m.getZonaId(),
            new Usuario.Membresia(
                m.getZonaId(),
                m.getRolZonal(),     // ya viene como enum
                m.getEstado(),       // ya viene como enum
                m.getCreatedAt()     // conserva timestamp
            )
        )
    );
    return u.build();
  }

  /** Helper para reconstrucción sin ctor público en el agregado */
  private static class UsuarioReflector {
    private final String id; private final String email; private final String nombre; private final String estado;
    private final java.util.Set<String> rolesGlobales = new java.util.HashSet<>();
    private final java.util.Map<String, Usuario.Membresia> membresias = new java.util.HashMap<>();
    UsuarioReflector(String id, String email, String nombre, String estado){
      this.id=id; this.email=email; this.nombre=nombre; this.estado=estado;
    }
    Usuario build(){
      var u = new UsuarioProxy(id, email, nombre, estado); // usa ctor protected del agregado
      u.getRolesGlobales().addAll(rolesGlobales);
      u.getMembresias().putAll(membresias);
      return u;
    }
    private static class UsuarioProxy extends Usuario {
      public UsuarioProxy(String id, String email, String nombre, String estado){
        super(id, email, nombre, estado);
      }
    }
  }
}