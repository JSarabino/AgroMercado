package com.agromercado.accounts.cmd.application.command.solicitud;

/**
 * Comando para que un admin de zona apruebe una solicitud de productor
 */
public record AprobarSolicitudProductorCommand(
    String solicitudId,
    String adminUsuarioId,  // Del JWT
    String observaciones
) {}
