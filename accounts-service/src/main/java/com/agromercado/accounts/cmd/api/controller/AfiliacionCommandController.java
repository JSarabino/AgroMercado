package com.agromercado.accounts.cmd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.cmd.api.dto.in.SolicitarAfiliacionRequest;
import com.agromercado.accounts.cmd.api.dto.out.AfiliacionResponse;
import com.agromercado.accounts.cmd.api.mapper.AfiliacionCommandMapper;
import com.agromercado.accounts.cmd.application.handler.SolicitarAfiliacionZonaHandler;
import com.agromercado.accounts.cmd.application.result.SolicitarAfiliacionZonaResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cmd/afiliaciones")
public class AfiliacionCommandController {

  private final SolicitarAfiliacionZonaHandler handler;
  private final AfiliacionCommandMapper mapper;

  public AfiliacionCommandController(SolicitarAfiliacionZonaHandler handler,
                                     AfiliacionCommandMapper mapper) {
    this.handler = handler;
    this.mapper = mapper;
  }

  @PostMapping("/solicitar")
  public ResponseEntity<AfiliacionResponse> solicitar(
      @Valid @RequestBody SolicitarAfiliacionRequest request,
      @AuthenticationPrincipal Jwt jwt
  ) {
    // 1) Requiere autenticación y subject presente
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 2) Mapear DTO -> Command (userId viene del JWT, NO del body)
    var command = mapper.toCommand(request, jwt.getSubject());

    // 3) Caso de uso (application)
    SolicitarAfiliacionZonaResult result = handler.handle(command);

    // 4) DTO de salida (API)
    var response = new AfiliacionResponse(
        result.afiliacionId(),
        result.zonaId(),
        result.mensaje()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
