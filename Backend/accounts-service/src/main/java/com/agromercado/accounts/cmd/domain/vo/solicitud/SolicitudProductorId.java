package com.agromercado.accounts.cmd.domain.vo.solicitud;

import java.util.UUID;

/** ID de Solicitud de Productor — String con prefijo SOLPROD- + UUID v4 */
public record SolicitudProductorId(String value) {
  public SolicitudProductorId {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("solicitud_id vacío");
    // validar formato: debe empezar por SOLPROD-
    if (!value.startsWith("SOLPROD-"))
      throw new IllegalArgumentException("solicitud_id debe iniciar con 'SOLPROD-'");
  }

  public static SolicitudProductorId newId() {
    return new SolicitudProductorId("SOLPROD-" + UUID.randomUUID());
  }
}
