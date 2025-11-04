package com.agromercado.accounts.cmd.api.dto.in;

/**
 * DTO para que un admin de zona apruebe o rechace una solicitud de productor
 */
public record DecisionSolicitudProductorRequest(
    String observaciones
) {}
