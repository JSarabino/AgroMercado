package com.agromercado.accounts.cmd.application.result;

/**
 * Resultado de la solicitud de afiliación de un productor a una zona
 */
public record SolicitarAfiliacionProductorResult(
    String afiliacionId,
    String zonaId,
    String mensaje
) {}
