package com.agromercado.accounts.qry.proyections;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.infrastructure.persistence.repository.SolicitudAfiliacionProductorJpaRepository;
import com.agromercado.accounts.qry.views.SolicitudProductorView;
import com.agromercado.accounts.qry.views.SolicitudProductorViewRepository;

/**
 * Projector que sincroniza solicitudes de productor desde PostgreSQL (Command) a MongoDB (Query).
 * En un sistema completo con eventos, esto se haría mediante event sourcing.
 * Por ahora usamos polling simple.
 */
@Component
public class SolicitudProductorProjector {

  private final SolicitudAfiliacionProductorJpaRepository cmdRepo;
  private final SolicitudProductorViewRepository qryRepo;

  public SolicitudProductorProjector(
      SolicitudAfiliacionProductorJpaRepository cmdRepo,
      SolicitudProductorViewRepository qryRepo
  ) {
    this.cmdRepo = cmdRepo;
    this.qryRepo = qryRepo;
  }

  /**
   * Sincroniza todas las solicitudes de PostgreSQL a MongoDB.
   * Se ejecuta cada 5 segundos (en producción usar eventos o CDC).
   */
  @Scheduled(fixedDelay = 5000)
  @Transactional(readOnly = true)
  public void syncSolicitudes() {
    var entities = cmdRepo.findAll();

    for (var entity : entities) {
      var viewOpt = qryRepo.findById(entity.getSolicitudId());

      // Si no existe o la versión es diferente, actualizar
      if (viewOpt.isEmpty() || viewOpt.get().getVersion() != entity.getVersion()) {
        var view = viewOpt.orElse(new SolicitudProductorView());

        view.setId(entity.getSolicitudId());
        view.setZonaId(entity.getZonaId());
        view.setProductorUsuarioId(entity.getProductorUsuarioId());
        view.setNombreProductor(entity.getNombreProductor());
        view.setDocumento(entity.getDocumento());
        view.setTelefono(entity.getTelefono());
        view.setCorreo(entity.getCorreo());
        view.setDireccion(entity.getDireccion());
        view.setTipoProductos(entity.getTipoProductos());
        view.setEstado(entity.getEstado());
        view.setObservaciones(entity.getObservaciones());
        view.setAprobadaPor(entity.getAprobadaPor());
        view.setFechaDecision(entity.getFechaDecision());
        view.setVersion(entity.getVersion());
        view.setCreatedAt(entity.getCreatedAt());
        view.setUpdatedAt(entity.getUpdatedAt());

        qryRepo.save(view);
      }
    }
  }
}
