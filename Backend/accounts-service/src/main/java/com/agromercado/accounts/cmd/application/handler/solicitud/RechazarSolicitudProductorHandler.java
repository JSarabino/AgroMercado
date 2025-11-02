package com.agromercado.accounts.cmd.application.handler.solicitud;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.solicitud.RechazarSolicitudProductorCommand;
import com.agromercado.accounts.cmd.domain.enum_.EstadoSolicitudProductor;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.SolicitudAfiliacionProductorJpaRepository;

/**
 * Handler para que un admin de zona rechace una solicitud de productor
 */
@Service
public class RechazarSolicitudProductorHandler {

  private final SolicitudAfiliacionProductorJpaRepository solicitudRepo;

  public RechazarSolicitudProductorHandler(SolicitudAfiliacionProductorJpaRepository solicitudRepo) {
    this.solicitudRepo = solicitudRepo;
  }

  @Transactional
  public void handle(RechazarSolicitudProductorCommand cmd) {

    // Buscar la solicitud
    var solicitud = solicitudRepo.findById(cmd.solicitudId())
        .orElseThrow(() -> new IllegalArgumentException(
            "No se encontró la solicitud con ID: " + cmd.solicitudId()
        ));

    // Validar que esté en estado PENDIENTE
    if (!EstadoSolicitudProductor.PENDIENTE.name().equals(solicitud.getEstado())) {
      throw new IllegalStateException(
          "Solo se pueden rechazar solicitudes en estado PENDIENTE. Estado actual: " + solicitud.getEstado()
      );
    }

    // Actualizar el estado
    solicitud.setEstado(EstadoSolicitudProductor.RECHAZADA.name());
    solicitud.setAprobadaPor(cmd.adminUsuarioId());
    solicitud.setFechaDecision(Instant.now());
    solicitud.setObservaciones(cmd.observaciones());
    solicitud.setVersion(solicitud.getVersion() + 1);

    solicitudRepo.save(solicitud);

    // TODO: Aquí podrías emitir un evento para notificar al productor
  }
}
