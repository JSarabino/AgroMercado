package com.agromercado.accounts.cmd.api.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload HTTP de entrada para que un productor solicite afiliarse a una zona existente
 */
public record SolicitarAfiliacionProductorRequest(

    @NotBlank @Size(max = 50)
    String zonaId,

    @NotBlank @Size(max = 120)
    String nombreProductor,

    @NotBlank @Size(max = 60)
    String documento,

    @Size(max = 40)
    String telefono,

    @NotBlank @Email @Size(max = 255)
    String correo,

    @Size(max = 255)
    String direccion,

    @Size(max = 500)
    String tipoProductos
) {}
