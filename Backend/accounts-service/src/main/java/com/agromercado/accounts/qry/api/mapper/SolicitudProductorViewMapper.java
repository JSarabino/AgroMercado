package com.agromercado.accounts.qry.api.mapper;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.qry.api.dto.SolicitudProductorResponseDTO;
import com.agromercado.accounts.qry.views.SolicitudProductorView;

@Component
public class SolicitudProductorViewMapper {

  public SolicitudProductorResponseDTO toResponse(SolicitudProductorView view) {
    var dto = new SolicitudProductorResponseDTO();
    dto.solicitudId = view.getId();
    dto.zonaId = view.getZonaId();
    dto.productorUsuarioId = view.getProductorUsuarioId();
    dto.nombreProductor = view.getNombreProductor();
    dto.documento = view.getDocumento();
    dto.telefono = view.getTelefono();
    dto.correo = view.getCorreo();
    dto.direccion = view.getDireccion();
    dto.tipoProductos = view.getTipoProductos();
    dto.estado = view.getEstado();
    dto.observaciones = view.getObservaciones();
    dto.aprobadaPor = view.getAprobadaPor();
    dto.fechaDecision = view.getFechaDecision();
    dto.createdAt = view.getCreatedAt();
    dto.updatedAt = view.getUpdatedAt();
    return dto;
  }
}
