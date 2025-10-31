package com.agromercado.accounts.cmd.domain.event.domain;

import java.util.Optional;

public record EventMetadata(
    String aggregateType,
    String aggregateId,
    String zonaId,
    int    aggregateVersion,
    Optional<String> causedByUserId,
    Optional<String> correlationId,
    Optional<String> causationId
) {
  public static EventMetadata of(
      String aggregateType, String aggregateId, String zonaId, int version,
      String userId, String correlationId, String causationId
  ) {
    return new EventMetadata(
        aggregateType, aggregateId, zonaId, version,
        Optional.ofNullable(userId),
        Optional.ofNullable(correlationId),
        Optional.ofNullable(causationId)
    );
  }
}