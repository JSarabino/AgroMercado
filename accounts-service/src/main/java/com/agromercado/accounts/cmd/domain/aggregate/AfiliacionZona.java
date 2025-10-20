package com.agromercado.accounts.cmd.domain.aggregate;

import java.util.ArrayList;
import java.util.List;

import com.agromercado.accounts.cmd.domain.enum_.EstadoAfiliacion;
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
