package com.agromercado.accounts.cmd.application.handler;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.application.command.RegistrarUsuarioCommand;
import com.agromercado.accounts.cmd.application.result.RegistrarUsuarioResult;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.UsuarioEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioJpaRepository;

import jakarta.transaction.Transactional;

@Service
public class RegistrarUsuarioHandler {

  private final UsuarioJpaRepository repo;

  public RegistrarUsuarioHandler(UsuarioJpaRepository repo) { this.repo = repo; }

  @Transactional
  public RegistrarUsuarioResult handle(RegistrarUsuarioCommand cmd) {
    repo.findByEmail(cmd.email()).ifPresent(u -> {
      throw new IllegalArgumentException("El email ya está registrado");
    });

    String usuarioId = "USR-" + UUID.randomUUID();
    var entity = new UsuarioEntity();
    entity.setUsuarioId(usuarioId);
    entity.setEmail(cmd.email());
    entity.setNombre(cmd.nombre());
    entity.setEstadoUsuario("ACTIVO");

    repo.save(entity);

    return new RegistrarUsuarioResult(usuarioId, cmd.email(), cmd.nombre(), "Usuario creado");
  }
}
