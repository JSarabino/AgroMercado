package com.agromercado.accounts.sync.contracts.usuario;

import java.time.Instant;

import com.agromercado.accounts.cmd.domain.enum_.EstadoMembresia;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;

public record MembresiaZonalOtorgadaMsg(
    String eventId, Instant occurredAt, EventMetadataDTO meta,
    String usuarioId, String zonaId, RolZonal rolZonal, EstadoMembresia estado
) {}