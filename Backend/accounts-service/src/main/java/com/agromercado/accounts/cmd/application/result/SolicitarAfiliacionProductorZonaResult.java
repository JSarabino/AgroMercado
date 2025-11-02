package com.agromercado.accounts.cmd.application.result;

/**
 * Resultado de crear una solicitud de afiliación de productor
 */
public record SolicitarAfiliacionProductorZonaResult(
    String solicitudId,
    String zonaId,
    String mensaje
) {}
