package com.agromercado.accounts.qry.proyections;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.qry.proyections.dto.AfiliacionSolicitadaDTO;
import com.agromercado.accounts.qry.views.AfiliacionZonaView;
import com.agromercado.accounts.qry.views.AfiliacionZonaViewRepository;

/**
 * Aplica eventos sobre la vista AfiliacionZonaView (read-model).
 * - Idempotencia por eventId
 * - Control de versión por aggregateVersion
 */
@Service
public class AfiliacionZonaProjector {

  private final AfiliacionZonaViewRepository viewRepo;
  private final ProcessedEventRepository processedRepo;
  private final Clock clock = Clock.systemUTC();

  public AfiliacionZonaProjector(AfiliacionZonaViewRepository viewRepo,
      ProcessedEventRepository processedRepo) {
    this.viewRepo = viewRepo;
    this.processedRepo = processedRepo;
  }

  @Transactional
  public void on(AfiliacionSolicitadaDTO evt) {
    // --- tomar campos desde meta ---
    var meta = evt.meta();
    String aggregateId = meta.aggregateId();
    String zonaId = meta.zonaId();
    Integer version = meta.aggregateVersion();
    String solicitanteId = meta.causedByUserId(); // puede venir null

    // 1) Idempotencia
    // Si tu ProcessedEvent.eventId es UUID, convierte aquí:
    // if (processedRepo.findByEventId(UUID.fromString(evt.eventId())).isPresent())
    // return;
    if (processedRepo.findByEventId(evt.eventId()).isPresent())
      return;

    // 2) Cargar vista actual (si existe)
    AfiliacionZonaView current = viewRepo.findById(aggregateId).orElse(null);

    // 3) Control de versión
    if (!ProjectionVersioning.isNewer(version, current)) {
      processedRepo.save(new ProcessedEvent(
          evt.eventId(), aggregateId, "AfiliacionSolicitada", Instant.now(clock)));
      return;
    }

    // 4) Upsert de la vista
    AfiliacionZonaView view = (current != null) ? current : new AfiliacionZonaView();
    view.setId(aggregateId);
    view.setZonaId(zonaId);
    view.setSolicitanteUsuarioId(solicitanteId); // puede quedar null
    view.setEstado("PENDIENTE");

    view.setNombreVereda(evt.nombreVereda());
    view.setMunicipio(evt.municipio());
    view.setRepresentanteNombre(evt.representanteNombre());
    view.setRepresentanteDocumento(evt.representanteDocumento());
    view.setRepresentanteCorreo(evt.representanteCorreo());

    view.setVersion(version);
    view.setFechaSolicitud(evt.occurredAt());
    view.setUpdatedAt(Instant.now(clock));

    viewRepo.save(view);

    // 5) Marcar procesado
    processedRepo.save(new ProcessedEvent(
        evt.eventId(), aggregateId, "AfiliacionSolicitada", Instant.now(clock)));
  }
}
