package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitudes_afiliacion_productor")
public class SolicitudAfiliacionProductorEntity {

  @Id
  @Column(name = "solicitud_id", length = 50)
  private String solicitudId;

  @Column(name = "zona_id", length = 50, nullable = false)
  private String zonaId;

  @Column(name = "productor_usuario_id", length = 120, nullable = false)
  private String productorUsuarioId;

  // Datos del productor
  @Column(name = "nombre_productor", length = 120, nullable = false)
  private String nombreProductor;

  @Column(name = "documento", length = 60, nullable = false)
  private String documento;

  @Column(name = "telefono", length = 40)
  private String telefono;

  @Column(name = "correo", length = 255)
  private String correo;

  @Column(name = "direccion", length = 255)
  private String direccion;

  @Column(name = "tipo_productos", length = 500)
  private String tipoProductos;

  // Estado de la solicitud
  @Column(name = "estado", length = 40, nullable = false)
  private String estado;

  @Column(name = "observaciones", columnDefinition = "TEXT")
  private String observaciones;

  // Auditoría
  @Column(name = "aprobada_por", length = 120)
  private String aprobadaPor;

  @Column(name = "fecha_decision")
  private Instant fechaDecision;

  @Column(name = "version", nullable = false)
  private int version;

  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SolicitudAfiliacionProductorEntity() {}

  public SolicitudAfiliacionProductorEntity(
      String solicitudId,
      String zonaId,
      String productorUsuarioId,
      String nombreProductor,
      String documento,
      String telefono,
      String correo,
      String direccion,
      String tipoProductos,
      String estado
  ) {
    this.solicitudId = solicitudId;
    this.zonaId = zonaId;
    this.productorUsuarioId = productorUsuarioId;
    this.nombreProductor = nombreProductor;
    this.documento = documento;
    this.telefono = telefono;
    this.correo = correo;
    this.direccion = direccion;
    this.tipoProductos = tipoProductos;
    this.estado = estado;
    this.version = 1;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
