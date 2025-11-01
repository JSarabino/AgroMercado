package com.agromercado.accounts.cmd.api.dto.in;

import com.agromercado.accounts.cmd.domain.enum_.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioRequest(
  @NotBlank @Email @Size(max = 255) String email,
  @NotBlank @Size(max = 120) String nombre,
  @NotBlank @Size(min = 6, max = 100) String password,
  @NotNull TipoUsuario tipoUsuario
) {}
