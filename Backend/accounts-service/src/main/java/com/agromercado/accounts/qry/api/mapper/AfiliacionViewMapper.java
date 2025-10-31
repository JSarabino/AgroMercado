package com.agromercado.accounts.qry.api.mapper;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.qry.api.dto.AfiliacionSolicitadaResponseDTO;
import com.agromercado.accounts.qry.views.AfiliacionZonaView;


@Component
public class AfiliacionViewMapper {

  public AfiliacionSolicitadaResponseDTO toResponseFull(AfiliacionZonaView v) {
    var r = base(v);
    r.representanteDocumento = v.getRepresentanteDocumento();
    r.representanteCorreo    = v.getRepresentanteCorreo();
    return r;
  }

  public AfiliacionSolicitadaResponseDTO toResponseRedacted(AfiliacionZonaView v) {
    var r = base(v);
    r.representanteDocumento = null;
    r.representanteCorreo    = null;
    return r;
  }

  private AfiliacionSolicitadaResponseDTO base(AfiliacionZonaView v) {
    var r = new AfiliacionSolicitadaResponseDTO();
    r.afiliacionId         = v.getId();                 // <-- clave
    r.zonaId               = v.getZonaId();
    r.solicitanteUsuarioId = v.getSolicitanteUsuarioId();
    r.estado               = v.getEstado();
    r.fechaSolicitud       = v.getFechaSolicitud();
    r.fechaDecision        = v.getFechaDecision();
    r.observaciones        = v.getObservaciones();
    r.nombreVereda         = v.getNombreVereda();
    r.municipio            = v.getMunicipio();
    r.representanteNombre  = v.getRepresentanteNombre();
    return r;
  }
}
