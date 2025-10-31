package com.agromercado.accounts.cmd.api.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioRequest(
  @NotBlank @Email @Size(max = 255) String email,
  @NotBlank @Size(max = 120) String nombre
) {}
