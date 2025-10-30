package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;


import java.time.Instant;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionAprobada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionRechazada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionSolicitada;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.EventMetadata;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;
import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;
import com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg;
import com.agromercado.accounts.sync.contracts.AfiliacionRechazadaMsg;
import com.agromercado.accounts.sync.contracts.AfiliacionSolicitadaMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


@Component
public class DomainEventMapper {

  private final ObjectMapper objectMapper;

  public DomainEventMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private static EventMetadataDTO toMetaDTO(EventMetadata m) {
    return new EventMetadataDTO(
        m.aggregateType(),
        m.aggregateId(),
        m.zonaId(),
        m.aggregateVersion(),        // int -> Integer (autoboxing)
        m.causedByUserId().orElse(null),
        m.correlationId().orElse(null),
        m.causationId().orElse(null)
    );
  }

  public OutboxEntity toEntity(DomainEvent evt) {
    try {
      final String eventType = evt.eventType();
      final String eventId;
      final String aggregateId;
      final String zonaId;
      final Instant occurredAt;
      final String payloadJson;

      if (evt instanceof AfiliacionSolicitada e) {
        eventId = e.eventId().toString();
        var metaDto = toMetaDTO(e.meta());
        var msg = new AfiliacionSolicitadaMsg(
            eventId, e.occurredAt(), metaDto,
            e.nombreVereda(), e.municipio(),
            e.representanteNombre(), e.representanteDocumento(), e.representanteCorreo()
        );
        payloadJson = objectMapper.writeValueAsString(msg);
        aggregateId = e.meta().aggregateId();
        zonaId = e.meta().zonaId();
        occurredAt = e.occurredAt();
      } else if (evt instanceof AfiliacionAprobada e) {
        eventId = e.eventId().toString();
        var metaDto = toMetaDTO(e.meta());
        var msg = new AfiliacionAprobadaMsg(
            eventId, e.occurredAt(), metaDto,
            e.meta().aggregateId(), e.meta().zonaId(),
            e.solicitanteUsuarioId(), e.observaciones(),
            "APROBADA"
        );
        payloadJson = objectMapper.writeValueAsString(msg);
        aggregateId = e.meta().aggregateId();
        zonaId = e.meta().zonaId();
        occurredAt = e.occurredAt();
      } else if (evt instanceof AfiliacionRechazada e) {
        eventId = e.eventId().toString();
        var metaDto = toMetaDTO(e.meta());
        var msg = new AfiliacionRechazadaMsg(
            eventId, e.occurredAt(), metaDto,
            e.meta().aggregateId(), e.meta().zonaId(),
            e.solicitanteUsuarioId(), e.observaciones(),
            "RECHAZADA"
        );
        payloadJson = objectMapper.writeValueAsString(msg);
        aggregateId = e.meta().aggregateId();
        zonaId = e.meta().zonaId();
        occurredAt = e.occurredAt();
      } else {
        // fallback
        eventId = java.util.UUID.randomUUID().toString();
        aggregateId = null;
        zonaId = null;
        occurredAt = Instant.now();
        payloadJson = objectMapper.writeValueAsString(evt);
      }

      return new OutboxEntity(
          eventId, eventType, payloadJson,
          aggregateId, zonaId,
          "PENDING",
          occurredAt,
          Instant.now() // createdAt
      );

    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Error serializando evento " + evt.eventType(), ex);
    }
  }
}