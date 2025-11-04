package com.agromercado.accounts.qry.views;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Vista de lectura para consultas de solicitudes de productor (CQRS Query).
 * Se sincroniza desde la tabla PostgreSQL solicitudes_afiliacion_productor.
 */
@Document(collection = "solicitudes_productor_view")
@CompoundIndex(def = "{'productorUsuarioId':1, 'zonaId':1}")
public class SolicitudProductorView {

  @Id
  private String id; // solicitudId

  @Indexed
  private String zonaId;

  @Indexed
  private String productorUsuarioId;

  // Datos del productor
  private String nombreProductor;
  private String documento;
  private String telefono;
  private String correo;
  private String direccion;
  private String tipoProductos;

  // Estado y auditoría
  @Indexed
  private String estado; // PENDIENTE | APROBADA | RECHAZADA
  private String observaciones;
  private String aprobadaPor;
  private Instant fechaDecision;

  // Control de versión
  private int version;
  private Instant createdAt;
  private Instant updatedAt;

  public SolicitudProductorView() {}

  // Getters y Setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getZonaId() { return zonaId; }
  public void setZonaId(String zonaId) { this.zonaId = zonaId; }

  public String getProductorUsuarioId() { return productorUsuarioId; }
  public void setProductorUsuarioId(String productorUsuarioId) { this.productorUsuarioId = productorUsuarioId; }

  public String getNombreProductor() { return nombreProductor; }
  public void setNombreProductor(String nombreProductor) { this.nombreProductor = nombreProductor; }

  public String getDocumento() { return documento; }
  public void setDocumento(String documento) { this.documento = documento; }

  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }

  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }

  public String getDireccion() { return direccion; }
  public void setDireccion(String direccion) { this.direccion = direccion; }

  public String getTipoProductos() { return tipoProductos; }
  public void setTipoProductos(String tipoProductos) { this.tipoProductos = tipoProductos; }

  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }

  public String getObservaciones() { return observaciones; }
  public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

  public String getAprobadaPor() { return aprobadaPor; }
  public void setAprobadaPor(String aprobadaPor) { this.aprobadaPor = aprobadaPor; }

  public Instant getFechaDecision() { return fechaDecision; }
  public void setFechaDecision(Instant fechaDecision) { this.fechaDecision = fechaDecision; }

  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
