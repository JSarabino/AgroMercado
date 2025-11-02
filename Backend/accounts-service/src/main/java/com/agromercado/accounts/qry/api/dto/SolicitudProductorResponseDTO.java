package com.agromercado.accounts.qry.api.dto;

import java.time.Instant;

/**
 * DTO de respuesta para consultas de solicitudes de productor
 */
public class SolicitudProductorResponseDTO {
  public String solicitudId;
  public String zonaId;
  public String productorUsuarioId;

  // Datos del productor
  public String nombreProductor;
  public String documento;
  public String telefono;
  public String correo;
  public String direccion;
  public String tipoProductos;

  // Estado
  public String estado;
  public String observaciones;
  public String aprobadaPor;
  public Instant fechaDecision;

  // Auditoría
  public Instant createdAt;
  public Instant updatedAt;
}
