package com.agromercado.accounts.cmd.domain.vo.afiliacion;

import com.agromercado.accounts.cmd.domain.enum_.RolZonal;

public record Representante(
    String nombre,
    String documento,
    String correo,
    RolZonal rolPropuesto
) {
  public Representante {
    if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre del representante requerido");
    if (documento == null || documento.isBlank()) throw new IllegalArgumentException("Documento del representante requerido");
    if (correo == null || !correo.contains("@")) throw new IllegalArgumentException("Correo del representante inválido");
    if (rolPropuesto == null || rolPropuesto != RolZonal.ADMIN_ZONA)
      throw new IllegalArgumentException("El rol propuesto debe ser ADMIN_ZONA");
  }
}
