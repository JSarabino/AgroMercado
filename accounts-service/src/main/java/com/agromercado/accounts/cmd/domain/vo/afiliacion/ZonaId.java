package com.agromercado.accounts.cmd.domain.vo.afiliacion;

import java.util.UUID;

/** ID de Zona — String con prefijo ZONA- + UUID v4 */
public record ZonaId(String value) {
  public ZonaId {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("zona_id vacío");
    if (!value.startsWith("ZONA-"))
      throw new IllegalArgumentException("zona_id debe iniciar con 'ZONA-'");
  }
  public static ZonaId newId() {
    return new ZonaId("ZONA-" + UUID.randomUUID());
  }
}
