package com.agromercado.accounts.qry.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.agromercado.accounts.qry.api.dto.SolicitudProductorResponseDTO;
import com.agromercado.accounts.qry.api.mapper.SolicitudProductorViewMapper;
import com.agromercado.accounts.qry.views.SolicitudProductorView;
import com.agromercado.accounts.qry.views.SolicitudProductorViewRepository;

/**
 * Controller de consultas (queries) para solicitudes de productor.
 * Permite a los admins de zona ver solicitudes pendientes de su zona,
 * y a los productores ver sus propias solicitudes.
 */
@RestController
@RequestMapping("/qry/solicitudes-productor")
public class SolicitudProductorQueryController {

  private final SolicitudProductorViewRepository repo;
  private final SolicitudProductorViewMapper mapper;

  public SolicitudProductorQueryController(
      SolicitudProductorViewRepository repo,
      SolicitudProductorViewMapper mapper
  ) {
    this.repo = repo;
    this.mapper = mapper;
  }

  /**
   * GET /qry/solicitudes-productor?zonaId=...&estado=...&productorId=...
   * - Si es ADMIN_ZONA: puede ver solicitudes de su zona
   * - Si es ADMIN_GLOBAL: puede ver todas las solicitudes
   * - Si es productor: solo puede ver sus propias solicitudes
   */
  @GetMapping
  public ResponseEntity<List<SolicitudProductorResponseDTO>> buscarSolicitudes(
      @RequestParam(required = false) String zonaId,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) String productorId,
      @RequestHeader(name = "X-User-Id", required = false) String callerUserId,
      @RequestHeader(name = "X-User-Roles", required = false) String callerRoles
  ) {
    boolean adminGlobal = hasRole(callerRoles, "ADMIN_GLOBAL");
    boolean adminZona = hasRole(callerRoles, "ADMIN_ZONA");

    List<SolicitudProductorView> views;

    // ADMIN_GLOBAL puede ver todo
    if (adminGlobal) {
      if (zonaId != null && !zonaId.isBlank() && estado != null && !estado.isBlank()) {
        views = repo.findByZonaIdAndEstado(zonaId, estado);
      } else if (zonaId != null && !zonaId.isBlank()) {
        views = repo.findByZonaId(zonaId);
      } else if (estado != null && !estado.isBlank()) {
        views = repo.findByEstado(estado);
      } else if (productorId != null && !productorId.isBlank()) {
        views = repo.findByProductorUsuarioId(productorId);
      } else {
        views = repo.findAll();
      }
    }
    // ADMIN_ZONA puede ver solicitudes de su zona (debe especificar zonaId)
    else if (adminZona) {
      if (zonaId == null || zonaId.isBlank()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(null); // Admin de zona debe especificar la zonaId
      }

      if (estado != null && !estado.isBlank()) {
        views = repo.findByZonaIdAndEstado(zonaId, estado);
      } else {
        views = repo.findByZonaId(zonaId);
      }
    }
    // Productor solo puede ver sus propias solicitudes
    else {
      if (callerUserId == null || callerUserId.isBlank()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      views = repo.findByProductorUsuarioId(callerUserId);
    }

    return ResponseEntity.ok(
        views.stream()
             .map(mapper::toResponse)
             .toList()
    );
  }

  /**
   * GET /qry/solicitudes-productor/{solicitudId}
   * Obtener detalle de una solicitud específica.
   */
  @GetMapping("/{solicitudId}")
  public ResponseEntity<SolicitudProductorResponseDTO> buscarDetalle(
      @PathVariable String solicitudId,
      @RequestHeader(name = "X-User-Id", required = false) String callerUserId,
      @RequestHeader(name = "X-User-Roles", required = false) String callerRoles
  ) {
    var viewOpt = repo.findById(solicitudId);
    if (viewOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    var view = viewOpt.get();
    boolean adminGlobal = hasRole(callerRoles, "ADMIN_GLOBAL");
    boolean adminZona = hasRole(callerRoles, "ADMIN_ZONA");

    // Validar permisos: admin global, admin zona, o el propio productor
    if (!adminGlobal && !adminZona && !view.getProductorUsuarioId().equals(callerUserId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(mapper.toResponse(view));
  }

  /**
   * GET /qry/solicitudes-productor/zona/{zonaId}/pendientes
   * Endpoint específico para admins de zona que quieren ver solicitudes pendientes.
   */
  @GetMapping("/zona/{zonaId}/pendientes")
  @PreAuthorize("hasRole('ADMIN_ZONA') or hasRole('ADMIN_GLOBAL')")
  public ResponseEntity<List<SolicitudProductorResponseDTO>> solicitudesPendientes(
      @PathVariable String zonaId
  ) {
    var views = repo.findByZonaIdAndEstado(zonaId, "PENDIENTE");
    return ResponseEntity.ok(
        views.stream()
             .map(mapper::toResponse)
             .toList()
    );
  }

  private boolean hasRole(String rolesHeader, String roleName) {
    if (rolesHeader == null || roleName == null) return false;
    for (String r : rolesHeader.split(",")) {
      if (roleName.equalsIgnoreCase(r.trim())) return true;
    }
    return false;
  }
}
