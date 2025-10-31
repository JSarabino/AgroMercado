package com.agromercado.accounts.cmd.domain.event.usuario;

import java.time.Instant;
import java.util.UUID;

import com.agromercado.accounts.cmd.domain.enum_.EstadoMembresia;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.domain.event.domain.EventMetadata;

public record MembresiaZonalOtorgada(
    UUID eventId, Instant occurredAt, EventMetadata meta,
    String usuarioId, String zonaId, RolZonal rolZonal, EstadoMembresia estado) implements DomainEvent {
  public static final String TYPE = "MembresiaZonalOtorgada";

  @Override
  public String eventType() {
    return TYPE;
  }
}
