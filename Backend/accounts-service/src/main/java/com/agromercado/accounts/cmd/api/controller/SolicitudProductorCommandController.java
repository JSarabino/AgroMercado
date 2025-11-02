package com.agromercado.accounts.cmd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.cmd.api.dto.in.DecisionSolicitudProductorRequest;
import com.agromercado.accounts.cmd.api.dto.in.SolicitarAfiliacionProductorZonaRequest;
import com.agromercado.accounts.cmd.api.dto.out.SolicitudProductorResponse;
import com.agromercado.accounts.cmd.application.command.solicitud.AprobarSolicitudProductorCommand;
import com.agromercado.accounts.cmd.application.command.solicitud.RechazarSolicitudProductorCommand;
import com.agromercado.accounts.cmd.application.command.solicitud.SolicitarAfiliacionProductorZonaCommand;
import com.agromercado.accounts.cmd.application.handler.solicitud.AprobarSolicitudProductorHandler;
import com.agromercado.accounts.cmd.application.handler.solicitud.RechazarSolicitudProductorHandler;
import com.agromercado.accounts.cmd.application.handler.solicitud.SolicitarAfiliacionProductorZonaHandler;
import com.agromercado.accounts.cmd.application.result.SolicitarAfiliacionProductorZonaResult;

import jakarta.validation.Valid;

/**
 * Controller para las solicitudes de afiliación de PRODUCTORES a ZONAS.
 * Este flujo es independiente del flujo de afiliación de zonas al admin global.
 */
@RestController
@RequestMapping("/cmd/solicitudes-productor")
public class SolicitudProductorCommandController {

  private final SolicitarAfiliacionProductorZonaHandler solicitarHandler;
  private final AprobarSolicitudProductorHandler aprobarHandler;
  private final RechazarSolicitudProductorHandler rechazarHandler;

  public SolicitudProductorCommandController(
      SolicitarAfiliacionProductorZonaHandler solicitarHandler,
      AprobarSolicitudProductorHandler aprobarHandler,
      RechazarSolicitudProductorHandler rechazarHandler
  ) {
    this.solicitarHandler = solicitarHandler;
    this.aprobarHandler = aprobarHandler;
    this.rechazarHandler = rechazarHandler;
  }

  /**
   * Endpoint para que un PRODUCTOR solicite afiliarse a una zona existente.
   * Requiere autenticación (cualquier usuario autenticado puede solicitar).
   */
  @PostMapping("/solicitar")
  public ResponseEntity<SolicitudProductorResponse> solicitarAfiliacion(
      @Valid @RequestBody SolicitarAfiliacionProductorZonaRequest request,
      @AuthenticationPrincipal Jwt jwt
  ) {
    // Validar autenticación
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Crear comando (el ID del productor viene del JWT)
    var command = new SolicitarAfiliacionProductorZonaCommand(
        jwt.getSubject(),
        request.zonaId(),
        request.nombreProductor(),
        request.documento(),
        request.telefono(),
        request.correo(),
        request.direccion(),
        request.tipoProductos()
    );

    // Ejecutar caso de uso
    SolicitarAfiliacionProductorZonaResult result = solicitarHandler.handle(command);

    // Respuesta
    var response = new SolicitudProductorResponse(
        result.solicitudId(),
        result.zonaId(),
        result.mensaje()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Endpoint para que un ADMIN_ZONA apruebe una solicitud de productor.
   * Solo el admin de la zona correspondiente puede aprobar.
   */
  @PostMapping("/{solicitudId}/aprobar")
  @PreAuthorize("hasRole('ADMIN_ZONA') or hasRole('ADMIN_GLOBAL')")
  public ResponseEntity<Void> aprobarSolicitud(
      @PathVariable String solicitudId,
      @RequestBody(required = false) DecisionSolicitudProductorRequest body,
      @AuthenticationPrincipal Jwt jwt
  ) {
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var observaciones = (body != null) ? body.observaciones() : null;

    var command = new AprobarSolicitudProductorCommand(
        solicitudId,
        jwt.getSubject(),
        observaciones
    );

    aprobarHandler.handle(command);

    return ResponseEntity.noContent().build();
  }

  /**
   * Endpoint para que un ADMIN_ZONA rechace una solicitud de productor.
   * Solo el admin de la zona correspondiente puede rechazar.
   */
  @PostMapping("/{solicitudId}/rechazar")
  @PreAuthorize("hasRole('ADMIN_ZONA') or hasRole('ADMIN_GLOBAL')")
  public ResponseEntity<Void> rechazarSolicitud(
      @PathVariable String solicitudId,
      @RequestBody(required = false) DecisionSolicitudProductorRequest body,
      @AuthenticationPrincipal Jwt jwt
  ) {
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var observaciones = (body != null) ? body.observaciones() : null;

    var command = new RechazarSolicitudProductorCommand(
        solicitudId,
        jwt.getSubject(),
        observaciones
    );

    rechazarHandler.handle(command);

    return ResponseEntity.noContent().build();
  }
}
