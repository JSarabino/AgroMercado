package com.agromercado.accounts.cmd.domain.event.afiliacion;

import java.time.Instant;
import java.util.UUID;

import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.EventMetadata;

public record AfiliacionRechazada(
    UUID eventId,
    Instant occurredAt,
    EventMetadata meta,
    String solicitanteUsuarioId,
    String observaciones
) implements DomainEvent {
  public static final String TYPE = "AfiliacionRechazada";
  @Override public String eventType() { return TYPE; }
}
