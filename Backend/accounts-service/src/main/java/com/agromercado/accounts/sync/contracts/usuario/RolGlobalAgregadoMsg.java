package com.agromercado.accounts.sync.contracts.usuario;

import java.time.Instant;

import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;

public record RolGlobalAgregadoMsg(
    String eventId, Instant occurredAt, EventMetadataDTO meta,
    String usuarioId, String rolGlobal
) {}
