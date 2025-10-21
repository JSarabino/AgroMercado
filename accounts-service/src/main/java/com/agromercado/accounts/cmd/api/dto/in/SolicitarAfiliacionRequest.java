package com.agromercado.accounts.cmd.api.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload HTTP de entrada (formulario de solicitud). */
public record SolicitarAfiliacionRequest(
    // Si aún no hay login, puede venir en el body; si hay JWT, lo ignoras y usas el subject
    String solicitanteUsuarioId,

    @NotBlank @Size(max = 120)
    String nombreVereda,

    @NotBlank @Size(max = 120)
    String municipio,

    @Size(max = 40)
    String telefono,

    @Email @Size(max = 255)
    String correo,

    @NotBlank @Size(max = 120)
    String representanteNombre,

    @NotBlank @Size(max = 60)
    String representanteDocumento,

    @NotBlank @Email @Size(max = 255)
    String representanteCorreo
) {}
