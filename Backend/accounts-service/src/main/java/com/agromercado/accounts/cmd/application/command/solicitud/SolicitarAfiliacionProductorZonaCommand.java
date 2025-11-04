package com.agromercado.accounts.cmd.application.command.solicitud;

/**
 * Comando para que un productor solicite afiliarse a una zona existente
 */
public record SolicitarAfiliacionProductorZonaCommand(
    String productorUsuarioId,  // Del JWT
    String zonaId,
    String nombreProductor,
    String documento,
    String telefono,
    String correo,
    String direccion,
    String tipoProductos
) {}
