package com.agromercado.accounts.sync.contracts;

import java.time.Instant;

import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;

public record AfiliacionSolicitadaMsg(
    String eventId,
    Instant occurredAt,
    EventMetadataDTO meta,
    String nombreVereda,
    String municipio,
    String representanteNombre,
    String representanteDocumento,
    String representanteCorreo
) {}