package com.agromercado.accounts.sync.contracts;

import java.time.Instant;

public record AfiliacionSolicitadaMsg(
    String eventId,
    Instant occurredAt,
    Meta meta,
    String nombreVereda,
    String municipio,
    String representanteNombre,
    String representanteDocumento,
    String representanteCorreo
) {
  public record Meta(
      String aggregateType,
      String aggregateId,  // == afiliacionId
      String zonaId,
      int aggregateVersion,
      String causedByUserId,
      String correlationId,
      String causationId
  ) {}
}