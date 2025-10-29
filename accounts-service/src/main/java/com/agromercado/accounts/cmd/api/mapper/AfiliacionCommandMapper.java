package com.agromercado.accounts.cmd.api.mapper;

import org.springframework.stereotype.Component;

import com.agromercado.accounts.cmd.api.dto.in.SolicitarAfiliacionRequest;
import com.agromercado.accounts.cmd.application.command.SolicitarAfiliacionZonaCommand;

/** Mapper fino: DTO HTTP -> Command de aplicación. */
@Component
public class AfiliacionCommandMapper {

  public SolicitarAfiliacionZonaCommand toCommand(SolicitarAfiliacionRequest dto, String solicitanteDesdeJwt) {
    if (solicitanteDesdeJwt == null || solicitanteDesdeJwt.isBlank()) {
      throw new IllegalArgumentException("Usuario autenticado requerido");
    }
    return new SolicitarAfiliacionZonaCommand(
        solicitanteDesdeJwt,
        dto.nombreVereda(),
        dto.municipio(),
        dto.telefono(),
        dto.correo(),
        dto.representanteNombre(),
        dto.representanteDocumento(),
        dto.representanteCorreo()
    );
  }
}
