package com.agromercado.accounts.qry.api.dto;

import java.time.Instant;

/** Respuesta de consulta de afiliaciones (HU03). */
public class AfiliacionSolicitadaResponseDTO {
    public String afiliacionId;
    public String zonaId;
    public String solicitanteUsuarioId;
    public String estado;

    public Instant fechaSolicitud;
    public Instant fechaDecision;
    public String observaciones;

    public String nombreVereda;
    public String municipio;
    public String representanteNombre;
    public String representanteDocumento; // puede ir null si se redacta
    public String representanteCorreo; // puede ir null si se redacta
}
