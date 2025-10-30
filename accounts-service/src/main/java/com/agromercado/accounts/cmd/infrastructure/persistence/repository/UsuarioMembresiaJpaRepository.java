package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioMembresiaEntity;

public interface UsuarioMembresiaJpaRepository extends JpaRepository<UsuarioMembresiaEntity, UsuarioMembresiaEntity.PK> {
  List<UsuarioMembresiaEntity> findByUsuarioId(String usuarioId);
}
