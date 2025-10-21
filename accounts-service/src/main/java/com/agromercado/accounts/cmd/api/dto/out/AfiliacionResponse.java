package com.agromercado.accounts.cmd.api.dto.out;

/** Respuesta HTTP al crear la solicitud. */
public record AfiliacionResponse(
    String afiliacionId,
    String zonaId,
    String mensaje
) {}
