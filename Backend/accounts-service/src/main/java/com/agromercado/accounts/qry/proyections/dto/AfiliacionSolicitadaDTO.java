package com.agromercado.accounts.qry.proyections.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO del evento en el lado QRY.
 * No dependemos del dominio CMD. Lo llena el suscriptor (sync) al deserializar
 * del mensaje.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AfiliacionSolicitadaDTO(
    String eventId,
    Instant occurredAt,
    EventMetadataDTO meta,          // <-- AQUÍ la diferencia clave
    String nombreVereda,
    String municipio,
    String representanteNombre,
    String representanteDocumento,
    String representanteCorreo
) {}
