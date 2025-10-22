package com.agromercado.accounts.qry.proyections.dto;

public record EventMetadataDTO(
    String aggregateType,
    String aggregateId,
    String zonaId,
    Integer aggregateVersion,
    String causedByUserId, // viene del Optional del CMD; aquí lo tratamos como String nullable
    String correlationId,
    String causationId
) {}