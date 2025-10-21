package com.agromercado.accounts.cmd.api.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.api.dto.in.SolicitarAfiliacionRequest;
import com.agromercado.accounts.cmd.application.command.SolicitarAfiliacionZonaCommand;

/** Mapper fino: DTO HTTP -> Command de aplicación. */
@Component
public class AfiliacionCommandMapper {

  public SolicitarAfiliacionZonaCommand toCommand(SolicitarAfiliacionRequest dto, String solicitanteDesdeJwt) {
    String solicitante = (solicitanteDesdeJwt != null && !solicitanteDesdeJwt.isBlank())
        ? solicitanteDesdeJwt
        : (dto.solicitanteUsuarioId() != null && !dto.solicitanteUsuarioId().isBlank()
            ? dto.solicitanteUsuarioId()
            : "temp-" + UUID.randomUUID());

    return new SolicitarAfiliacionZonaCommand(
        solicitante,
        dto.nombreVereda(),
        dto.municipio(),
        dto.telefono(),
        dto.correo(),
        dto.representanteNombre(),
        dto.representanteDocumento(),
        dto.representanteCorreo());
  }
}
