package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;

import java.time.Instant;

import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.AfiliacionZonaEntity;

public final class AfiliacionZonaMapper {

  private AfiliacionZonaMapper() {}

  public static AfiliacionZonaEntity toEntity(AfiliacionZona agg) {
    // Extraer VOs del agregado
    var datos = agg.getDatosZona();
    var contacto = datos.contacto();
    var rep = datos.representante();

    return new AfiliacionZonaEntity(
        agg.getAfiliacionId().value(),
        agg.getZonaId().value(),
        agg.getSolicitanteUsuarioId(),
        datos.nombreVereda(),
        datos.municipio(),
        contacto != null ? contacto.telefono() : null,
        contacto != null ? contacto.correo() : null,
        rep != null ? rep.nombre() : null,
        rep != null ? rep.documento() : null,
        rep != null ? rep.correo() : null,
        agg.getEstado().name(),
        agg.getVersion(),
        Instant.now() // o un createdAt que venga del agregado si lo tienes
    );
  }
}
