package com.agromercado.accounts.cmd.domain.event.usuario;

import java.time.Instant;
import java.util.UUID;

import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.EventMetadata;

public record RolGlobalAgregado(
    UUID eventId, Instant occurredAt, EventMetadata meta,
    String usuarioId, String rolGlobal) implements DomainEvent {
  public static final String TYPE = "RolGlobalAgregado";

  @Override
  public String eventType() {
    return TYPE;
  }
}