package com.agromercado.accounts.cmd.domain.vo.afiliacion;

public record DatosZona(
    String nombreVereda,
    String municipio,
    Contacto contacto,
    Representante representante
) {
  public DatosZona {
    if (nombreVereda == null || nombreVereda.isBlank()) throw new IllegalArgumentException("Nombre de vereda requerido");
    if (municipio == null || municipio.isBlank())       throw new IllegalArgumentException("Municipio requerido");
    if (contacto == null)       throw new IllegalArgumentException("Contacto requerido");
    if (representante == null)  throw new IllegalArgumentException("Representante requerido");
  }
}
