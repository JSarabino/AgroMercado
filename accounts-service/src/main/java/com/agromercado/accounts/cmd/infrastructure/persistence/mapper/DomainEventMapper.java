package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;


import java.time.Instant;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DomainEventMapper {

  private final ObjectMapper mapper; // el de Spring ya trae JavaTimeModule

  public DomainEventMapper(ObjectMapper mapper) { this.mapper = mapper; }

  public OutboxEntity toEntity(DomainEvent event) {
    try {
      String payload = mapper.writeValueAsString(event);
      var meta = event.meta();
      return new OutboxEntity(
          event.eventId().toString(),
          event.eventType(),
          payload,
          meta.aggregateId(),
          meta.zonaId(),
          "PENDING",
          event.occurredAt(),
          Instant.now()
      );
    } catch (Exception e) {
      throw new RuntimeException("Error serializando evento: " + event.eventType(), e);
    }
  }
}
