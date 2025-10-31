package com.agromercado.accounts.qry.proyections;

import com.agromercado.accounts.qry.views.AfiliacionZonaView;

/**
 * Regla de control de versión para evitar aplicar eventos viejos sobre la vista.
 */
final class ProjectionVersioning {

  private ProjectionVersioning() {}

  static boolean isNewer(Integer incomingVersion, AfiliacionZonaView current) {
    if (incomingVersion == null) return true; // si no llega versión, asumimos aplicar
    Integer cur = current != null ? current.getVersion() : null;
    return cur == null || incomingVersion >= cur; // permitir igual/greater (idempotente)
  }
}
