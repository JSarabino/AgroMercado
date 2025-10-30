package com.agromercado.accounts.cmd.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Optional;

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
    var existing = repo.findById(aggregate.getAfiliacionId().value());
    Instant createdAt = existing.map(e -> e.getCreatedAt()).orElse(Instant.now());
    var entity = AfiliacionZonaMapper.toEntity(aggregate, createdAt);
    repo.save(entity);
  }

  @Override
  public Optional<AfiliacionZona> findById(String afiliacionId) {
    return repo.findById(afiliacionId).map(AfiliacionZonaMapper::toAggregate);
  }
}
