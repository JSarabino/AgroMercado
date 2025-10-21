package com.agromercado.accounts.cmd.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.application.port.out.AfiliacionZonaInterface;
import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;
import com.agromercado.accounts.cmd.infrastructure.persistence.mapper.AfiliacionZonaMapper;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.AfiliacionZonaJpaRepository;

@Component
public class AfiliacionZonaJpaImpl implements AfiliacionZonaInterface {

  private final AfiliacionZonaJpaRepository repo;

  public AfiliacionZonaJpaImpl(AfiliacionZonaJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public void save(AfiliacionZona aggregate) {
    var entity = AfiliacionZonaMapper.toEntity(aggregate);
    repo.save(entity);
  }
}
