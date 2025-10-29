package com.agromercado.accounts.cmd.api.dto.out;

public record UsuarioResponse(
  String usuarioId,
  String email,
  String nombre,
  String mensaje
) {}
