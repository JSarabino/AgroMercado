package com.agromercado.accounts.qry.views;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Vista de lectura para consultas (CQRS).
 * id = afiliacionId (mismo id del agregado en CMD).
 */
@Document(collection = "afiliaciones_zona_view")
@CompoundIndex(def = "{'solicitanteUsuarioId':1, 'zonaId':1}")
public class AfiliacionZonaView {

  @Id
  private String id; // afiliacionId
  @Indexed
  private String zonaId;

  @Indexed
  private String solicitanteUsuarioId;

  private String nombreVereda;
  private String municipio;

  // Datos del representante
  private String representanteNombre;
  private String representanteDocumento;
  private String representanteCorreo;

  // Estado y auditoría de la solicitud
  private String estado;              // PENDIENTE | APROBADA | RECHAZADA
  private Instant fechaSolicitud;     // occurredAt del evento de creación
  private Instant fechaDecision;      // se llenará cuando haya aprobación/rechazo
  private String observaciones;       // se llenará cuando haya decisión

  // Control de concurrencia/idempotencia de la proyección
  private int version;                // versión del agregado
  private Instant updatedAt;

  public AfiliacionZonaView() {}

  // Getters/Setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getZonaId() { return zonaId; }
  public void setZonaId(String zonaId) { this.zonaId = zonaId; }

  public String getSolicitanteUsuarioId() { return solicitanteUsuarioId; }
  public void setSolicitanteUsuarioId(String solicitanteUsuarioId) { this.solicitanteUsuarioId = solicitanteUsuarioId; }

  public String getNombreVereda() { return nombreVereda; }
  public void setNombreVereda(String nombreVereda) { this.nombreVereda = nombreVereda; }

  public String getMunicipio() { return municipio; }
  public void setMunicipio(String municipio) { this.municipio = municipio; }

  public String getRepresentanteNombre() { return representanteNombre; }
  public void setRepresentanteNombre(String representanteNombre) { this.representanteNombre = representanteNombre; }

  public String getRepresentanteDocumento() { return representanteDocumento; }
  public void setRepresentanteDocumento(String representanteDocumento) { this.representanteDocumento = representanteDocumento; }

  public String getRepresentanteCorreo() { return representanteCorreo; }
  public void setRepresentanteCorreo(String representanteCorreo) { this.representanteCorreo = representanteCorreo; }

  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }

  public Instant getFechaSolicitud() { return fechaSolicitud; }
  public void setFechaSolicitud(Instant fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

  public Instant getFechaDecision() { return fechaDecision; }
  public void setFechaDecision(Instant fechaDecision) { this.fechaDecision = fechaDecision; }

  public String getObservaciones() { return observaciones; }
  public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
