package com.agromercado.accounts.qry.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.qry.api.dto.AfiliacionSolicitadaResponseDTO;
import com.agromercado.accounts.qry.api.mapper.AfiliacionViewMapper;
import com.agromercado.accounts.qry.views.AfiliacionZonaView;
import com.agromercado.accounts.qry.views.AfiliacionZonaViewRepository;

@RestController
@RequestMapping("/qry/afiliaciones")
public class AfiliacionQueryController {

  private final AfiliacionZonaViewRepository repo;
  private final AfiliacionViewMapper mapper;

  public AfiliacionQueryController(AfiliacionZonaViewRepository repo, AfiliacionViewMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  /**
   * GET /qry/afiliaciones?solicitante=... | ?zonaId=... | (sin params si ADMIN_GLOBAL, ADMIN_ZONA o PRODUCTOR)
   * - Si es ADMIN_GLOBAL sin parámetros: devuelve TODAS las afiliaciones
   * - Si es ADMIN_ZONA/PRODUCTOR/CLIENTE sin parámetros: devuelve solo las APROBADAS (para seleccionar zona)
   * - Si tiene parámetros: filtra por solicitante o zonaId
   * Redacción: si el caller no es solicitante ni ADMIN_GLOBAL, ocultamos datos sensibles.
   */
  @GetMapping
  public ResponseEntity<?> buscarColeccion(
      @RequestParam(required = false) String solicitante,
      @RequestParam(required = false) String zonaId,
      @RequestHeader(name = "X-User-Id", required = false) String callerUserId,
      @RequestHeader(name = "X-User-Roles", required = false) String callerRoles
  ) {
    boolean adminGlobal = hasAdminGlobal(callerRoles);
    boolean isAdminZona = hasRole(callerRoles, "ADMIN_ZONA");
    boolean isProductorOrCliente = hasRole(callerRoles, "PRODUCTOR") || hasRole(callerRoles, "CLIENTE");

    // Si no hay parámetros
    if ((solicitante == null || solicitante.isBlank()) && (zonaId == null || zonaId.isBlank())) {
      // ADMIN_GLOBAL: devolver TODAS las afiliaciones
      if (adminGlobal) {
        List<AfiliacionZonaView> views = repo.findAll();
        return ResponseEntity.ok(
            views.stream()
                 .map(mapper::toResponseFull)
                 .toList()
        );
      }

      // ADMIN_ZONA, PRODUCTOR o CLIENTE: devolver solo las APROBADAS (para que puedan seleccionar zona)
      if (isAdminZona || isProductorOrCliente) {
        List<AfiliacionZonaView> views = repo.findByEstado("APROBADA");
        return ResponseEntity.ok(
            views.stream()
                 .map(mapper::toResponseFull)
                 .toList()
        );
      }

      // Otros usuarios: deben especificar parámetros
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Debe especificar solicitante o zonaId");
    }

    // Con parámetros: filtrar por solicitante o zonaId
    List<AfiliacionZonaView> views =
        (solicitante != null && !solicitante.isBlank())
            ? repo.findBySolicitanteUsuarioId(solicitante)
            : repo.findByZonaId(zonaId);

    return ResponseEntity.ok(
        views.stream()
             .map(v -> canSeeFull(v, callerUserId, adminGlobal)
                 ? mapper.toResponseFull(v)
                 : mapper.toResponseRedacted(v))
             .toList()
    );
  }

  /**
   * GET /qry/afiliaciones/{afiliacionId}
   * Devuelve un solo recurso; aplica redacción igual que el listado.
   */
  @GetMapping("/{afiliacionId}")
  public ResponseEntity<?> buscarDetalle(
      @PathVariable String afiliacionId,
      @RequestHeader(name = "X-User-Id", required = false) String callerUserId,
      @RequestHeader(name = "X-User-Roles", required = false) String callerRoles
  ) {
    var viewOpt = repo.findById(afiliacionId);
    if (viewOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    var view = viewOpt.get();
    boolean adminGlobal = hasAdminGlobal(callerRoles);

    AfiliacionSolicitadaResponseDTO resp = canSeeFull(view, callerUserId, adminGlobal)
        ? mapper.toResponseFull(view)
        : mapper.toResponseRedacted(view);

    return ResponseEntity.ok(resp);
  }

  private boolean canSeeFull(AfiliacionZonaView v, String callerUserId, boolean adminGlobal) {
    if (adminGlobal) return true;
    if (callerUserId == null || callerUserId.isBlank()) return false;
    return callerUserId.equals(v.getSolicitanteUsuarioId());
  }

  private boolean hasAdminGlobal(String rolesHeader) {
    return hasRole(rolesHeader, "ADMIN_GLOBAL");
  }

  private boolean hasRole(String rolesHeader, String roleName) {
    if (rolesHeader == null || roleName == null) return false;
    // Ejemplo: "USER,ADMIN_GLOBAL" o "ADMIN_GLOBAL" o "PRODUCTOR"
    for (String r : rolesHeader.split(",")) {
      if (roleName.equalsIgnoreCase(r.trim())) return true;
    }
    return false;
  }
}
