package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;


import java.time.Instant;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionAprobada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionRechazada;
import com.agromercado.accounts.cmd.domain.event.afiliacion.AfiliacionSolicitada;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.EventMetadata;
import com.agromercado.accounts.cmd.domain.event.usuario.MembresiaZonalOtorgada;
import com.agromercado.accounts.cmd.domain.event.usuario.RolGlobalAgregado;
import com.agromercado.accounts.cmd.domain.event.usuario.UsuarioRegistrado;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;
import com.agromercado.accounts.qry.proyections.dto.EventMetadataDTO;
import com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg;
import com.agromercado.accounts.sync.contracts.AfiliacionRechazadaMsg;
import com.agromercado.accounts.sync.contracts.AfiliacionSolicitadaMsg;
import com.agromercado.accounts.sync.contracts.usuario.MembresiaZonalOtorgadaMsg;
import com.agromercado.accounts.sync.contracts.usuario.RolGlobalAgregadoMsg;
import com.agromercado.accounts.sync.contracts.usuario.UsuarioRegistradoMsg;
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
        m.aggregateVersion(),
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

      // ====== Eventos de USUARIO ======
      if (evt instanceof UsuarioRegistrado e) {
        var meta = toMetaDTO(e.meta());
        var msg = new UsuarioRegistradoMsg(
            e.eventId().toString(), e.occurredAt(), meta,
            e.usuarioId(), e.email(), e.nombre()
        );
        return new OutboxEntity(
            e.eventId().toString(), e.eventType(),
            objectMapper.writeValueAsString(msg),
            meta.aggregateId(), meta.zonaId(),
            "PENDING", e.occurredAt(), Instant.now()
        );
      }

      if (evt instanceof RolGlobalAgregado e) {
        var meta = toMetaDTO(e.meta());
        var msg = new RolGlobalAgregadoMsg(
            e.eventId().toString(), e.occurredAt(), meta,
            e.usuarioId(), e.rolGlobal()
        );
        return new OutboxEntity(
            e.eventId().toString(), e.eventType(),
            objectMapper.writeValueAsString(msg),
            meta.aggregateId(), meta.zonaId(),
            "PENDING", e.occurredAt(), Instant.now()
        );
      }

      if (evt instanceof MembresiaZonalOtorgada e) {
        var meta = toMetaDTO(e.meta());
        var msg = new MembresiaZonalOtorgadaMsg(
            e.eventId().toString(), e.occurredAt(), meta,
            e.usuarioId(), e.zonaId(), e.rolZonal(), e.estado()
        );
        return new OutboxEntity(
            e.eventId().toString(), e.eventType(),
            objectMapper.writeValueAsString(msg),
            meta.aggregateId(), meta.zonaId(),
            "PENDING", e.occurredAt(), Instant.now()
        );
      }

      // ====== Eventos de AFILIACIÓN ======
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
        // Fallback genérico (no deberías caer aquí normalmente)
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
          Instant.now()
      );

    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Error serializando evento " + evt.eventType(), ex);
    }
  }
}