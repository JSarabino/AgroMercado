package com.agromercado.accounts.cmd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.cmd.api.dto.in.AgregarRolGlobalRequest;
import com.agromercado.accounts.cmd.api.dto.in.RegistrarUsuarioRequest;
import com.agromercado.accounts.cmd.api.dto.out.UsuarioResponse;
import com.agromercado.accounts.cmd.application.command.RegistrarUsuarioCommand;
import com.agromercado.accounts.cmd.application.command.usuario.AgregarRolGlobalCommand;
import com.agromercado.accounts.cmd.application.command.usuario.OtorgarMembresiaZonalCommand;
import com.agromercado.accounts.cmd.application.handler.AgregarRolGlobalHandler;
import com.agromercado.accounts.cmd.application.handler.OtorgarMembresiaZonalHandler;
import com.agromercado.accounts.cmd.application.handler.RegistrarUsuarioHandler;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@RestController
@RequestMapping("/cmd/usuarios")
public class UsuarioCommandController {

  private final RegistrarUsuarioHandler registrar;
  private final AgregarRolGlobalHandler addRol;
  private final OtorgarMembresiaZonalHandler addMem;

  public UsuarioCommandController(RegistrarUsuarioHandler r,
                                  AgregarRolGlobalHandler a,
                                  OtorgarMembresiaZonalHandler m) {
    this.registrar = r;
    this.addRol = a;
    this.addMem = m;
  }

  // Crea usuario "normal" (sin roles globales) - público según SecurityConfig
  @PostMapping
  public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest body) {
    var id = registrar.handle(new RegistrarUsuarioCommand(body.email(), body.nombre()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UsuarioResponse(id, body.email(), body.nombre(), "Usuario creado"));
  }

  // Agregar rol global (ADMIN_GLOBAL). Protegido por @PreAuthorize
  @PatchMapping("/{usuarioId}/roles-globales")
  @PreAuthorize("hasRole('ADMIN_GLOBAL')")
  public ResponseEntity<String> addRolGlobal(@PathVariable String usuarioId,
                                             @Valid @RequestBody AgregarRolGlobalRequest body,
                                             @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    var actor = jwt.getSubject();
    addRol.handle(new AgregarRolGlobalCommand(usuarioId, body.rolGlobal(), actor));
    return ResponseEntity.ok("Rol global agregado: " + body.rolGlobal());
  }

  // (Opcional para pruebas manuales) Otorgar membresía zonal. En prod lo hace el subscriber.
  @PostMapping("/{usuarioId}/membresias")
  @PreAuthorize("hasRole('ADMIN_GLOBAL')")
  public ResponseEntity<String> addMembresia(@PathVariable String usuarioId,
                                             @Valid @RequestBody OtorgarMembresiaRequest body,
                                             @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    addMem.handle(new OtorgarMembresiaZonalCommand(
        usuarioId,
        body.zonaId(),
        body.rolZonal(),
        jwt.getSubject()     // causedBy: admin que ejecuta la acción
    ));
    return ResponseEntity.ok("Membresía otorgada");
  }

  // ===== DTO local para pruebas manuales de membresía =====
  public static final class OtorgarMembresiaRequest {
    @NotBlank private String zonaId;
    @NotBlank private String rolZonal; // "ADMIN_ZONA" | "PRODUCTOR"

    public String zonaId() { return zonaId; }
    public void setZonaId(String z) { this.zonaId = z; }

    public RolZonal rolZonal() { return RolZonal.valueOf(rolZonal); }
    public void setRolZonal(String r) { this.rolZonal = r; }
  }
}