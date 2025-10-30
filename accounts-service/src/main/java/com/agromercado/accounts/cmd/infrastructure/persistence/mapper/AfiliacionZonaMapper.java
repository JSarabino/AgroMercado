package com.agromercado.accounts.cmd.infrastructure.persistence.mapper;

import java.time.Instant;

import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;
import com.agromercado.accounts.cmd.domain.enum_.EstadoAfiliacion;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.AfiliacionId;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.Contacto;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.DatosZona;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.Representante;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.ZonaId;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.AfiliacionZonaEntity;

public final class AfiliacionZonaMapper {

  private AfiliacionZonaMapper() {}

  // SOBRECARGA RECOMENDADA para preservar createdAt
  public static AfiliacionZonaEntity toEntity(AfiliacionZona agg, Instant createdAt) {
    var datos = agg.getDatosZona();
    var c = datos.contacto();
    var r = datos.representante();

    return new AfiliacionZonaEntity(
        agg.getAfiliacionId().value(),
        agg.getZonaId().value(),
        agg.getSolicitanteUsuarioId(),
        datos.nombreVereda(),
        datos.municipio(),
        c != null ? c.telefono() : null,
        c != null ? c.correo()   : null,
        r != null ? r.nombre()   : null,
        r != null ? r.documento(): null,
        r != null ? r.correo()   : null,
        agg.getEstado().name(),
        agg.getVersion(),
        createdAt
    );
  }

  // NUEVO: Entity -> Aggregate
  public static AfiliacionZona toAggregate(AfiliacionZonaEntity e) {
    var contacto = new Contacto(e.getTelefono(), e.getCorreo());
    // En HU01 el representante propone ADMIN_ZONA (si prefieres persistirlo, mapéalo desde DB)
    var representante = new Representante(
        e.getRepresentanteNombre(),
        e.getRepresentanteDocumento(),
        e.getRepresentanteCorreo(),
        RolZonal.ADMIN_ZONA
    );
    var datos = new DatosZona(e.getNombreVereda(), e.getMunicipio(), contacto, representante);

    return AfiliacionZona.rehydrate(
        new AfiliacionId(e.getAfiliacionId()),
        new ZonaId(e.getZonaId()),
        e.getSolicitanteUsuarioId(),
        datos,
        EstadoAfiliacion.valueOf(e.getEstado()),
        e.getVersion()
    );
  }
}
