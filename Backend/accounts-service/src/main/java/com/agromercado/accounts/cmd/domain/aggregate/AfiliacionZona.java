package com.agromercado.accounts.cmd.domain.aggregate;

import java.util.ArrayList;
import java.util.List;

import com.agromercado.accounts.cmd.domain.enum_.EstadoAfiliacion;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionAprobada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionRechazada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionSolicitada;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.AfiliacionId;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.DatosZona;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.ZonaId;

public class AfiliacionZona {

  // Atributos
  private final AfiliacionId afiliacionId;
  private final ZonaId zonaId;
  private final String solicitanteUsuarioId;
  private DatosZona datosZona;
  private EstadoAfiliacion estado;
  private int version;
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  // Constructor
  private AfiliacionZona(AfiliacionId afiId,
      ZonaId zonaId,
      String solicitanteUsuarioId,
      DatosZona datosZona) {
    this.afiliacionId = afiId;
    this.zonaId = zonaId;
    this.solicitanteUsuarioId = solicitanteUsuarioId;
    this.datosZona = datosZona;
    this.estado = EstadoAfiliacion.PENDIENTE;
    this.version = 1;
  }

  public static AfiliacionZona rehydrate(
      AfiliacionId afiliacionId,
      ZonaId zonaId,
      String solicitanteUsuarioId,
      DatosZona datosZona,
      EstadoAfiliacion estado,
      int version
  ) {
    var agg = new AfiliacionZona(afiliacionId, zonaId, solicitanteUsuarioId, datosZona);
    agg.estado = estado;
    agg.version = version;
    return agg;
  }

  /** HU01: crear afiliación en PENDIENTE y elevar AfiliacionSolicitada. */
  public static AfiliacionZona solicitar(String solicitanteUsuarioId, DatosZona datos, DomainEvents factory) {
    if (solicitanteUsuarioId == null || solicitanteUsuarioId.isBlank())
      throw new IllegalArgumentException("Solicitante requerido");

    var afiId = AfiliacionId.newId();
    var zonaId = ZonaId.newId();
    var agg = new AfiliacionZona(afiId, zonaId, solicitanteUsuarioId, datos);

    var meta = factory.newMeta(
        "AfiliacionZona",
        afiId.value(),
        zonaId.value(),
        agg.version,
        solicitanteUsuarioId,
        null, // correlationId
        null // causationId
    );

    agg.domainEvents.add(new AfiliacionSolicitada(
        factory.newEventId(),
        factory.now(),
        meta,
        datos.nombreVereda(),
        datos.municipio(),
        datos.representante().nombre(),
        datos.representante().documento(),
        datos.representante().correo()));

    return agg;
  }

  /** HU02: aprobar afiliación. */
  public AfiliacionAprobada aprobar(String adminGlobalId, String observaciones, DomainEvents factory) {
    ensureEstado(EstadoAfiliacion.PENDIENTE);
    this.estado = EstadoAfiliacion.APROBADA;
    var meta = factory.newMeta("AfiliacionZona", afiliacionId.value(), zonaId.value(),
        this.version + 1, adminGlobalId, null, null);

    var evt = new AfiliacionAprobada(
        factory.newEventId(),
        factory.now(),
        meta,
        solicitanteUsuarioId,
        observaciones
    );
    this.version++;
    this.domainEvents.add(evt);
    return evt;
  }

  /** HU02: rechazar afiliación. */
  public AfiliacionRechazada rechazar(String adminGlobalId, String observaciones, DomainEvents factory) {
    ensureEstado(EstadoAfiliacion.PENDIENTE);
    this.estado = EstadoAfiliacion.RECHAZADA;
    var meta = factory.newMeta("AfiliacionZona", afiliacionId.value(), zonaId.value(),
        this.version + 1, adminGlobalId, null, null);

    var evt = new AfiliacionRechazada(
        factory.newEventId(),
        factory.now(),
        meta,
        solicitanteUsuarioId,
        observaciones
    );
    this.version++;
    this.domainEvents.add(evt);
    return evt;
  }

  private void ensureEstado(EstadoAfiliacion esperado) {
    if (this.estado != esperado) {
      throw new IllegalStateException("Transición inválida desde " + this.estado);
    }
  }

  public List<DomainEvent> pullDomainEvents() {
    var copy = List.copyOf(domainEvents);
    domainEvents.clear();
    return copy;
  }

  // Getters
  public AfiliacionId getAfiliacionId() {
    return afiliacionId;
  }

  public ZonaId getZonaId() {
    return zonaId;
  }

  public String getSolicitanteUsuarioId() {
    return solicitanteUsuarioId;
  }

  public DatosZona getDatosZona() {
    return datosZona;
  }

  public EstadoAfiliacion getEstado() {
    return estado;
  }

  public int getVersion() {
    return version;
  }
}
