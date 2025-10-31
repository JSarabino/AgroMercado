package com.agromercado.accounts.cmd.infrastructure.persistence.adapter;


import java.util.List;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvent;
import com.agromercado.accounts.cmd.infrastructure.persistence.mapper.DomainEventMapper;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.OutboxJpaRepository;

@Component
public class OutboxJpaImpl implements OutboxInterface {

  private final OutboxJpaRepository repo;
  private final DomainEventMapper mapper;

  public OutboxJpaImpl(OutboxJpaRepository repo, DomainEventMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Override
  public void saveAll(List<DomainEvent> events) {
    var entities = events.stream().map(mapper::toEntity).toList();
    repo.saveAll(entities);
  }
}