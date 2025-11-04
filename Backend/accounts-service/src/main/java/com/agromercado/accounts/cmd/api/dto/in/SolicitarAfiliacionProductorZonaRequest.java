package com.agromercado.accounts.cmd.api.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para que un productor solicite afiliarse a una zona existente.
 * El ID del productor viene del JWT, no del body.
 */
public record SolicitarAfiliacionProductorZonaRequest(
    @NotBlank(message = "zonaId es obligatorio")
    String zonaId,

    @NotBlank(message = "nombreProductor es obligatorio")
    String nombreProductor,

    @NotBlank(message = "documento es obligatorio")
    String documento,

    String telefono,

    @Email(message = "correo debe ser válido")
    String correo,

    String direccion,

    String tipoProductos // Puede ser JSON o texto simple
) {}
