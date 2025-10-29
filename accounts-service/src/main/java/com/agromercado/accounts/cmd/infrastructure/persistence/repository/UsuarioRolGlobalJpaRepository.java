package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioRolGlobalEntity;

public interface UsuarioRolGlobalJpaRepository
    extends JpaRepository<UsuarioRolGlobalEntity, UsuarioRolGlobalEntity.PK> {
}
