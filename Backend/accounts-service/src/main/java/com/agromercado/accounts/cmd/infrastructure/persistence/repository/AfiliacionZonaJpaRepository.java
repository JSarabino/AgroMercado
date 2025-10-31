package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.AfiliacionZonaEntity;

public interface AfiliacionZonaJpaRepository extends JpaRepository<AfiliacionZonaEntity, String> {}
