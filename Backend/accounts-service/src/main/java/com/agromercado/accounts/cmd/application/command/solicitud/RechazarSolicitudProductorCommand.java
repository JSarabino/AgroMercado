package com.agromercado.accounts.cmd.application.command.solicitud;

/**
 * Comando para que un admin de zona rechace una solicitud de productor
 */
public record RechazarSolicitudProductorCommand(
    String solicitudId,
    String adminUsuarioId,  // Del JWT
    String observaciones
) {}
