package com.agromercado.accounts.cmd.application.handler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.RechazarAfiliacionCommand;
import com.agromercado.accounts.cmd.application.port.out.AfiliacionZonaInterface;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

@Service
public class RechazarAfiliacionHandler {

  private final AfiliacionZonaInterface afiliacionStore;
  private final OutboxInterface outboxStore;
  private final DomainEvents domainEvents;

  public RechazarAfiliacionHandler(
      AfiliacionZonaInterface afiliacionStore,
      OutboxInterface outboxStore,
      DomainEvents domainEvents
  ) {
    this.afiliacionStore = afiliacionStore;
    this.outboxStore = outboxStore;
    this.domainEvents = domainEvents;
  }

  @Transactional
  public void handle(RechazarAfiliacionCommand cmd) {
    var agg = afiliacionStore.findById(cmd.afiliacionId())
        .orElseThrow(() -> new IllegalArgumentException("Afiliación no encontrada: " + cmd.afiliacionId()));

    // muta el agregado (valida estado internamente y emite evento)
    agg.rechazar(cmd.adminGlobalId(), cmd.observaciones(), domainEvents);

    // persiste y envía eventos a outbox en la misma tx
    afiliacionStore.save(agg);
    outboxStore.saveAll(agg.pullDomainEvents());
  }
}
