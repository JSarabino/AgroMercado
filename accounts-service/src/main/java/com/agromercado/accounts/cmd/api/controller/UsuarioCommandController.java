package com.agromercado.accounts.cmd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.cmd.api.dto.in.AgregarRolGlobalRequest;
import com.agromercado.accounts.cmd.api.dto.in.RegistrarUsuarioRequest;
import com.agromercado.accounts.cmd.api.dto.out.UsuarioResponse;
import com.agromercado.accounts.cmd.application.command.RegistrarUsuarioCommand;
import com.agromercado.accounts.cmd.application.handler.AgregarRolGlobalHandler;
import com.agromercado.accounts.cmd.application.handler.RegistrarUsuarioHandler;
import com.agromercado.accounts.cmd.application.result.RegistrarUsuarioResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cmd/usuarios")
public class UsuarioCommandController {

  private final RegistrarUsuarioHandler registrarUsuario;
  private final AgregarRolGlobalHandler agregarRolGlobal;

  public UsuarioCommandController(RegistrarUsuarioHandler r, AgregarRolGlobalHandler a) {
    this.registrarUsuario = r; this.agregarRolGlobal = a;
  }

  /** Crea un usuario normal (sin roles globales) */
  @PostMapping
  public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
    RegistrarUsuarioResult res = registrarUsuario.handle(
        new RegistrarUsuarioCommand(request.email(), request.nombre())
    );
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UsuarioResponse(res.usuarioId(), res.email(), res.nombre(), res.mensaje()));
  }

  /**
   * Elevar a ADMIN_GLOBAL (ruta DEV). En producción debería exigir que el caller
   * tenga rol ADMIN_GLOBAL. Para MVP puedes dejarla abierta o usar sólo el bootstrap V3.
   */
  @PostMapping("/{usuarioId}/roles-globales")
  public ResponseEntity<String> addRolGlobal(
      @PathVariable String usuarioId,
      @Valid @RequestBody AgregarRolGlobalRequest body
  ) {
    agregarRolGlobal.add(usuarioId, body.rolGlobal()); // "ADMIN_GLOBAL"
    return ResponseEntity.ok("Rol global agregado: " + body.rolGlobal());
  }
}
