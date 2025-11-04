package com.agromercado.accounts.cmd.application.port.out;

import java.util.Optional;

import com.agromercado.accounts.cmd.domain.aggregate.Usuario;

public interface UsuarioInterface {
  Optional<Usuario> findById(String usuarioId);
  Optional<Usuario> findByEmail(String email);
  boolean existsByEmail(String email);
  void save(Usuario usuario);
}
