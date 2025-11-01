package com.agromercado.accounts.cmd.api.dto.out;

import java.util.List;

public record LoginResponse(
  String accessToken,
  String tokenType,
  long expiresIn,
  String usuarioId,
  String email,
  String nombre,
  String tipoUsuario,
  List<String> rolesGlobales,
  List<String> membresias
) {}
