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
    var meta = evt.meta();
    String aggregateId = meta.aggregateId();
    String zonaId = meta.zonaId();
    Integer version = meta.aggregateVersion();
    String solicitanteId = meta.causedByUserId();

    if (processedRepo.findByEventId(evt.eventId()).isPresent()) return;

    AfiliacionZonaView current = viewRepo.findById(aggregateId).orElse(null);

    if (!ProjectionVersioning.isNewer(version, current)) {
      processedRepo.save(new ProcessedEvent(evt.eventId(), aggregateId, "AfiliacionSolicitada", Instant.now(clock)));
      return;
    }

    AfiliacionZonaView view = (current != null) ? current : new AfiliacionZonaView();
    view.setId(aggregateId);
    view.setZonaId(zonaId);
    view.setSolicitanteUsuarioId(solicitanteId);
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
    processedRepo.save(new ProcessedEvent(evt.eventId(), aggregateId, "AfiliacionSolicitada", Instant.now(clock)));
  }

  @Transactional
  public void onAprobada(com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg evt) {
    var meta = evt.meta();
    String eventId = evt.eventId();
    String afiliacionId = evt.afiliacionId();
    Integer version = meta.aggregateVersion();

    if (processedRepo.findByEventId(eventId).isPresent()) return;

    AfiliacionZonaView current = viewRepo.findById(afiliacionId)
        .orElseThrow(() -> new ProyectionException("View no encontrada: " + afiliacionId));

    if (!ProjectionVersioning.isNewer(version, current)) {
      processedRepo.save(new ProcessedEvent(eventId, afiliacionId, "AfiliacionAprobada", Instant.now(clock)));
      return;
    }

    current.setEstado("APROBADA");
    current.setVersion(version);
    current.setUpdatedAt(evt.occurredAt());

    viewRepo.save(current);
    processedRepo.save(new ProcessedEvent(eventId, afiliacionId, "AfiliacionAprobada", Instant.now(clock)));
  }

  @Transactional
  public void onRechazada(com.agromercado.accounts.sync.contracts.AfiliacionRechazadaMsg evt) {
    var meta = evt.meta();
    String eventId = evt.eventId();
    String afiliacionId = evt.afiliacionId();
    Integer version = meta.aggregateVersion();

    if (processedRepo.findByEventId(eventId).isPresent()) return;

    AfiliacionZonaView current = viewRepo.findById(afiliacionId)
        .orElseThrow(() -> new ProyectionException("View no encontrada: " + afiliacionId));

    if (!ProjectionVersioning.isNewer(version, current)) {
      processedRepo.save(new ProcessedEvent(eventId, afiliacionId, "AfiliacionRechazada", Instant.now(clock)));
      return;
    }

    current.setEstado("RECHAZADA");
    current.setVersion(version);
    current.setUpdatedAt(evt.occurredAt());

    viewRepo.save(current);
    processedRepo.save(new ProcessedEvent(eventId, afiliacionId, "AfiliacionRechazada", Instant.now(clock)));
  }
}



















