package com.agromercado.accounts.cmd.domain.vo.afiliacion;

public record Contacto(String telefono, String correo) {
  public Contacto {
    if ((telefono == null || telefono.isBlank()) && (correo == null || correo.isBlank())) {
      throw new IllegalArgumentException("Debe existir teléfono o correo de contacto");
    }
    if (correo != null && !correo.isBlank() && !correo.contains("@")) {
      throw new IllegalArgumentException("Correo de contacto inválido");
    }
  }
}
