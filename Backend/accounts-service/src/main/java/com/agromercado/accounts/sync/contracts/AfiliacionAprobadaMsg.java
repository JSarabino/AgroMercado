package com.agromercado.accounts.sync.contracts;

import java.time.Instant;

import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;

public record AfiliacionAprobadaMsg(
    String eventId,
    Instant occurredAt,
    EventMetadataDTO meta,
    String afiliacionId,
    String zonaId,
    String solicitanteUsuarioId,
    String observaciones,
    String nuevoEstado // "APROBADA"
) {}
