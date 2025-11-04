package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, String> {
  boolean existsByEmail(String email);
  Optional<UsuarioEntity> findByEmail(String email);
}
