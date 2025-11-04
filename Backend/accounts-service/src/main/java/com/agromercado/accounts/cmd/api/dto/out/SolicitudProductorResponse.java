package com.agromercado.accounts.cmd.api.dto.out;

/**
 * Respuesta al crear una solicitud de afiliación de productor
 */
public record SolicitudProductorResponse(
    String solicitudId,
    String zonaId,
    String mensaje
) {}
