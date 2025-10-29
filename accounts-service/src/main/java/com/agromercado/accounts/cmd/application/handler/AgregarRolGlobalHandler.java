package com.agromercado.accounts.cmd.application.handler;

import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioRolGlobalEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioJpaRepository;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioRolGlobalJpaRepository;

import jakarta.transaction.Transactional;

@Service
public class AgregarRolGlobalHandler {
  private final UsuarioJpaRepository usuarios;
  private final UsuarioRolGlobalJpaRepository roles;

  public AgregarRolGlobalHandler(UsuarioJpaRepository u, UsuarioRolGlobalJpaRepository r) {
    this.usuarios = u; this.roles = r;
  }

  @Transactional
  public void add(String usuarioId, String rolGlobal) {
    var user = usuarios.findById(usuarioId)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    var e = new UsuarioRolGlobalEntity();
    e.setUsuarioId(user.getUsuarioId());
    e.setRolGlobal(rolGlobal);
    roles.save(e);
  }
}
