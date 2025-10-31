package com.agromercado.accounts.sync.contracts.usuario;

import java.time.Instant;

import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;

public record UsuarioRegistradoMsg(
    String eventId, Instant occurredAt, EventMetadataDTO meta,
    String usuarioId, String email, String nombre
) {}
