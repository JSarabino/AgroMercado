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
   * GET /qry/afiliaciones?solicitante=... | ?zonaId=...
   * Al menos uno de los dos parámetros debe venir.
   * Redacción: si el caller no es solicitante ni ADMIN_GLOBAL, ocultamos datos sensibles.
   */
  @GetMapping
  public ResponseEntity<?> buscarColeccion(
      @RequestParam(required = false) String solicitante,
      @RequestParam(required = false) String zonaId,
      @RequestHeader(name = "X-User-Id", required = false) String callerUserId,
      @RequestHeader(name = "X-User-Roles", required = false) String callerRoles
  ) {
    if ((solicitante == null || solicitante.isBlank()) && (zonaId == null || zonaId.isBlank())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Debe especificar solicitante o zonaId");
    }

    List<AfiliacionZonaView> views =
        (solicitante != null && !solicitante.isBlank())
            ? repo.findBySolicitanteUsuarioId(solicitante)
            : repo.findByZonaId(zonaId);

    boolean adminGlobal = hasAdminGlobal(callerRoles);
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
    if (rolesHeader == null) return false;
    // Ejemplo: "USER,ADMIN_GLOBAL" o "ADMIN_GLOBAL"
    for (String r : rolesHeader.split(",")) {
      if ("ADMIN_GLOBAL".equalsIgnoreCase(r.trim())) return true;
    }
    return false;
  }
}
