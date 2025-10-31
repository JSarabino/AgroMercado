package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "afiliaciones_zona")
public class AfiliacionZonaEntity {

  @Id
  @Column(name = "afiliacion_id", length = 50)
  private String afiliacionId;

  @Column(name = "zona_id", length = 50, nullable = false)
  private String zonaId;

  @Column(name = "solicitante_usuario_id", nullable = false)
  private String solicitanteUsuarioId;

  @Column(name = "nombre_vereda", length = 120, nullable = false)
  private String nombreVereda;

  @Column(name = "municipio", length = 120, nullable = false)
  private String municipio;

  @Column(name = "telefono", length = 40)
  private String telefono;

  @Column(name = "correo", length = 255)
  private String correo;

  @Column(name = "representante_nombre", length = 120, nullable = false)
  private String representanteNombre;

  @Column(name = "representante_documento", length = 60, nullable = false)
  private String representanteDocumento;

  @Column(name = "representante_correo", length = 255, nullable = false)
  private String representanteCorreo;

  @Column(name = "estado", length = 40, nullable = false)
  private String estado;

  @Column(name = "version", nullable = false)
  private int version;

  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  protected AfiliacionZonaEntity() {}

  public AfiliacionZonaEntity(String afiliacionId, String zonaId, String solicitanteUsuarioId,
                              String nombreVereda, String municipio, String telefono,
                              String correo, String representanteNombre,
                              String representanteDocumento, String representanteCorreo,
                              String estado, int version, Instant createdAt) {
    this.afiliacionId = afiliacionId;
    this.zonaId = zonaId;
    this.solicitanteUsuarioId = solicitanteUsuarioId;
    this.nombreVereda = nombreVereda;
    this.municipio = municipio;
    this.telefono = telefono;
    this.correo = correo;
    this.representanteNombre = representanteNombre;
    this.representanteDocumento = representanteDocumento;
    this.representanteCorreo = representanteCorreo;
    this.estado = estado;
    this.version = version;
    this.createdAt = createdAt;
  }

  // getters y setters omitidos por brevedad
}