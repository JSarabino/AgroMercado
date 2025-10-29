package com.agromercado.accounts.cmd.api.dto.in;

import jakarta.validation.constraints.NotBlank;

public record AgregarRolGlobalRequest(
  @NotBlank String rolGlobal // "ADMIN_GLOBAL"
) {}
