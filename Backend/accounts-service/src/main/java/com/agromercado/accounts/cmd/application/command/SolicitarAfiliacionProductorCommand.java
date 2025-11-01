package com.agromercado.accounts.cmd.application.command;

/**
 * Comando para que un productor solicite afiliarse a una zona existente
 */
public record SolicitarAfiliacionProductorCommand(
    String productorUsuarioId,  // ID del usuario productor (viene del JWT)
    String zonaId,              // ID de la zona a la que quiere afiliarse
    String nombreProductor,
    String documento,
    String telefono,
    String correo,
    String direccion,
    String tipoProductos
) {}
