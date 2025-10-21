package com.agromercado.accounts.cmd.application.command;

/** Datos del formulario/solicitante que llegan del API (DTO → Command). */
public record SolicitarAfiliacionZonaCommand(
    String solicitanteUsuarioId,  // puede venir del JWT o generado temporalmente
    String nombreVereda,
    String municipio,
    String telefono,   
    String correo,         
    String representanteNombre,
    String representanteDocumento,
    String representanteCorreo
) {}
