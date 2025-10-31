package com.agromercado.accounts.cmd.application.port.out;

import java.util.List;

import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;

/** Puerto para guardar eventos en la Outbox (misma TX que el agregado). */
public interface OutboxInterface {
  void saveAll(List<DomainEvent> events);
}
