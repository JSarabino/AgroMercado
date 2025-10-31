package com.agromercado.accounts.cmd.domain.event.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class DomainEvents {
  
  private final Clock clock;

  public DomainEvents(Clock clock) { this.clock = clock; }

  public Instant now() { return clock.instant(); }

  public UUID newEventId() { return UUID.randomUUID(); }

  public EventMetadata newMeta(
      String aggregateType, String aggregateId, String zonaId, int version,
      String userId, String correlationId, String causationId
  ) {
    return EventMetadata.of(aggregateType, aggregateId, zonaId, version, userId, correlationId, causationId);
  }
}
