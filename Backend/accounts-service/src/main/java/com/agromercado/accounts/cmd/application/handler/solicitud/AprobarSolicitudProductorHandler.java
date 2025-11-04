package com.agromercado.accounts.cmd.application.handler.solicitud;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.solicitud.AprobarSolicitudProductorCommand;
import com.agromercado.accounts.cmd.application.command.usuario.OtorgarMembresiaZonalCommand;
import com.agromercado.accounts.cmd.application.handler.OtorgarMembresiaZonalHandler;
import com.agromercado.accounts.cmd.domain.enum_.EstadoSolicitudProductor;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.SolicitudAfiliacionProductorJpaRepository;

/**
 * Handler para que un admin de zona apruebe una solicitud de productor
 */
@Service
public class AprobarSolicitudProductorHandler {

  private final SolicitudAfiliacionProductorJpaRepository solicitudRepo;
  private final OtorgarMembresiaZonalHandler membresiaHandler;

  public AprobarSolicitudProductorHandler(
      SolicitudAfiliacionProductorJpaRepository solicitudRepo,
      OtorgarMembresiaZonalHandler membresiaHandler
  ) {
    this.solicitudRepo = solicitudRepo;
    this.membresiaHandler = membresiaHandler;
  }

  @Transactional
  public void handle(AprobarSolicitudProductorCommand cmd) {

    // Buscar la solicitud
    var solicitud = solicitudRepo.findById(cmd.solicitudId())
        .orElseThrow(() -> new IllegalArgumentException(
            "No se encontró la solicitud con ID: " + cmd.solicitudId()
        ));

    // Validar que esté en estado PENDIENTE
    if (!EstadoSolicitudProductor.PENDIENTE.name().equals(solicitud.getEstado())) {
      throw new IllegalStateException(
          "Solo se pueden aprobar solicitudes en estado PENDIENTE. Estado actual: " + solicitud.getEstado()
      );
    }

    // Actualizar el estado
    solicitud.setEstado(EstadoSolicitudProductor.APROBADA.name());
    solicitud.setAprobadaPor(cmd.adminUsuarioId());
    solicitud.setFechaDecision(Instant.now());
    solicitud.setObservaciones(cmd.observaciones());
    solicitud.setVersion(solicitud.getVersion() + 1);

    solicitudRepo.save(solicitud);

    // Otorgar membresía automáticamente al productor en la zona
    membresiaHandler.handle(new OtorgarMembresiaZonalCommand(
        solicitud.getProductorUsuarioId(),
        solicitud.getZonaId(),
        RolZonal.PRODUCTOR,
        cmd.adminUsuarioId() // quien aprobó la solicitud
    ));
  }
}
