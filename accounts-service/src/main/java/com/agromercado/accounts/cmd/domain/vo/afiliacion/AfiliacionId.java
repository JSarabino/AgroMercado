package com.agromercado.accounts.cmd.domain.vo.afiliacion;

import java.util.UUID;

/** ID de Afiliación — String con prefijo AFI- + UUID v4 */
public record AfiliacionId(String value) {
  public AfiliacionId {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("afiliacion_id vacío");
    // validar formato: debe empezar por AFI-
    if (!value.startsWith("AFI-"))
      throw new IllegalArgumentException("afiliacion_id debe iniciar con 'AFI-'");
  }
  public static AfiliacionId newId() {
    return new AfiliacionId("AFI-" + UUID.randomUUID());
  }
}
