package com.agromercado.accounts.cmd.application.handler;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.SolicitarAfiliacionProductorCommand;
import com.agromercado.accounts.cmd.application.result.SolicitarAfiliacionProductorResult;
import com.agromercado.accounts.qry.views.AfiliacionZonaView;
import com.agromercado.accounts.qry.views.AfiliacionZonaViewRepository;

/**
 * Handler para solicitudes de productores que quieren afiliarse a una zona existente.
 * Solución simplificada: guardamos directamente en la vista de MongoDB
 */
@Service
public class SolicitarAfiliacionProductorHandler {

  private final AfiliacionZonaViewRepository afiliacionViewRepo;

  public SolicitarAfiliacionProductorHandler(AfiliacionZonaViewRepository afiliacionViewRepo) {
    this.afiliacionViewRepo = afiliacionViewRepo;
  }

  /**
   * Procesa la solicitud del productor y la guarda directamente en MongoDB
   */
  @Transactional
  public SolicitarAfiliacionProductorResult handle(SolicitarAfiliacionProductorCommand cmd) {

    // Validaciones básicas
    if (cmd.productorUsuarioId() == null || cmd.productorUsuarioId().isBlank()) {
      throw new IllegalArgumentException("El ID del productor es requerido");
    }

    if (cmd.zonaId() == null || cmd.zonaId().isBlank()) {
      throw new IllegalArgumentException("El ID de la zona es requerido");
    }

    // Verificar que la zona exista y esté aprobada
    var zonasAprobadas = afiliacionViewRepo.findByZonaId(cmd.zonaId());
    if (zonasAprobadas.isEmpty()) {
      throw new IllegalArgumentException("La zona especificada no existe o no está aprobada");
    }

    var zonaExistente = zonasAprobadas.get(0);
    if (!"APROBADA".equals(zonaExistente.getEstado())) {
      throw new IllegalArgumentException("Solo puedes afiliarte a zonas que estén aprobadas");
    }

    // Crear una nueva solicitud de afiliación del productor
    var afiliacionId = "AFP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    var now = Instant.now();

    var solicitudProductor = new AfiliacionZonaView();
    solicitudProductor.setId(afiliacionId); // El @Id es 'id', no 'afiliacionId'
    solicitudProductor.setEstado("PENDIENTE");
    solicitudProductor.setZonaId(cmd.zonaId());
    solicitudProductor.setSolicitanteUsuarioId(cmd.productorUsuarioId());
    solicitudProductor.setNombreVereda(zonaExistente.getNombreVereda());
    solicitudProductor.setMunicipio(zonaExistente.getMunicipio());
    solicitudProductor.setRepresentanteNombre(cmd.nombreProductor());
    solicitudProductor.setRepresentanteDocumento(cmd.documento());
    solicitudProductor.setRepresentanteCorreo(cmd.correo());
    solicitudProductor.setFechaSolicitud(now);
    solicitudProductor.setUpdatedAt(now);
    solicitudProductor.setVersion(1);

    // Guardar en MongoDB
    afiliacionViewRepo.save(solicitudProductor);

    // Retornar resultado
    return new SolicitarAfiliacionProductorResult(
        afiliacionId,
        cmd.zonaId(),
        "Solicitud de afiliación enviada. El administrador de la zona la revisará pronto."
    );
  }
}
