package com.agromercado.accounts.cmd.application.handler.solicitud;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.solicitud.SolicitarAfiliacionProductorZonaCommand;
import com.agromercado.accounts.cmd.application.result.SolicitarAfiliacionProductorZonaResult;
import com.agromercado.accounts.cmd.domain.enum_.EstadoSolicitudProductor;
import com.agromercado.accounts.cmd.domain.vo.solicitud.SolicitudProductorId;
import com.agromercado.accounts.cmd.infrastructure.persistence.entity.SolicitudAfiliacionProductorEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.SolicitudAfiliacionProductorJpaRepository;

/**
 * Handler para que un productor solicite afiliarse a una zona existente
 */
@Service
public class SolicitarAfiliacionProductorZonaHandler {

  private final SolicitudAfiliacionProductorJpaRepository solicitudRepo;

  public SolicitarAfiliacionProductorZonaHandler(SolicitudAfiliacionProductorJpaRepository solicitudRepo) {
    this.solicitudRepo = solicitudRepo;
  }

  @Transactional
  public SolicitarAfiliacionProductorZonaResult handle(SolicitarAfiliacionProductorZonaCommand cmd) {

    // Validaciones
    if (cmd.productorUsuarioId() == null || cmd.productorUsuarioId().isBlank()) {
      throw new IllegalArgumentException("El ID del productor es requerido");
    }

    if (cmd.zonaId() == null || cmd.zonaId().isBlank()) {
      throw new IllegalArgumentException("El ID de la zona es requerido");
    }

    // Verificar si ya existe una solicitud pendiente del mismo productor a la misma zona
    var solicitudExistente = solicitudRepo.findByProductorUsuarioIdAndZonaIdAndEstado(
        cmd.productorUsuarioId(),
        cmd.zonaId(),
        EstadoSolicitudProductor.PENDIENTE.name()
    );

    if (solicitudExistente.isPresent()) {
      throw new IllegalStateException(
          "Ya existe una solicitud pendiente de este productor para la zona " + cmd.zonaId()
      );
    }

    // Crear nueva solicitud
    var solicitudId = SolicitudProductorId.newId();

    var entity = new SolicitudAfiliacionProductorEntity(
        solicitudId.value(),
        cmd.zonaId(),
        cmd.productorUsuarioId(),
        cmd.nombreProductor(),
        cmd.documento(),
        cmd.telefono(),
        cmd.correo(),
        cmd.direccion(),
        cmd.tipoProductos(),
        EstadoSolicitudProductor.PENDIENTE.name()
    );

    solicitudRepo.save(entity);

    return new SolicitarAfiliacionProductorZonaResult(
        solicitudId.value(),
        cmd.zonaId(),
        "Solicitud de afiliación enviada correctamente"
    );
  }
}
