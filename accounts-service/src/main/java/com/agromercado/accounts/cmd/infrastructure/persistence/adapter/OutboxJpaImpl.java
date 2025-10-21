package com.agromercado.accounts.cmd.infrastructure.persistence.adapter;


import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.infrastructure.persistence.mapper.DomainEventMapper;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.OutboxJpaRepository;

@Component
public class OutboxJpaImpl implements OutboxInterface {

  private final OutboxJpaRepository outboxRepo;
  private final DomainEventMapper eventMapper;

  public OutboxJpaImpl(OutboxJpaRepository outboxRepo, DomainEventMapper eventMapper) {
    this.outboxRepo = outboxRepo;
    this.eventMapper = eventMapper;
  }

  @Override
  @Transactional
  public void saveAll(List<DomainEvent> events) {
    var rows = events.stream().map(eventMapper::toEntity).toList();
    outboxRepo.saveAll(rows);
  }
}
