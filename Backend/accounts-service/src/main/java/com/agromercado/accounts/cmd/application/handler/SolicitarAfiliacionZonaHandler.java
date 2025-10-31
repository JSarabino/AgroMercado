package com.agromercado.accounts.cmd.application.handler;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.SolicitarAfiliacionZonaCommand;
import com.agromercado.accounts.cmd.application.port.out.AfiliacionZonaInterface;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.application.result.SolicitarAfiliacionZonaResult;
import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.Contacto;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.DatosZona;
import com.agromercado.accounts.cmd.domain.vo.afiliacion.Representante;

@Service
public class SolicitarAfiliacionZonaHandler {

  private final AfiliacionZonaInterface afiliacionStore;
  private final OutboxInterface outboxStore;
  private final DomainEvents domainEventsFactory;

  // Inyectamos un Clock para tests deterministas
  public SolicitarAfiliacionZonaHandler(AfiliacionZonaInterface afiliacionStore,
      OutboxInterface outboxStore) {
    this.afiliacionStore = afiliacionStore;
    this.outboxStore = outboxStore;
    this.domainEventsFactory = new DomainEvents(Clock.systemUTC());
  }

  /**
   * Caso de uso HU01. En una sola TX: crear agregado, persistir, capturar eventos
   * en outbox.
   */
  @Transactional
  public SolicitarAfiliacionZonaResult handle(SolicitarAfiliacionZonaCommand cmd) {

    // 1) Validaciones de aplicación (autorización, precondiciones simples)
    if (cmd.solicitanteUsuarioId() == null || cmd.solicitanteUsuarioId().isBlank()) {
      throw new IllegalArgumentException("solicitanteUsuarioId es requerido");
    }

    // 2) Armar VOs (validación de forma va dentro de cada VO)
    var contacto = new Contacto(cmd.telefono(), cmd.correo());
    var representante = new Representante(
        cmd.representanteNombre(),
        cmd.representanteDocumento(),
        cmd.representanteCorreo(),
        RolZonal.ADMIN_ZONA // según HU01 el rol propuesto debe ser admin_zona
    );
    var datosZona = new DatosZona(cmd.nombreVereda(), cmd.municipio(), contacto, representante);

    // 3) Crear el agregado por su FÁBRICA de dominio (emite DomainEvent)
    var agg = AfiliacionZona.solicitar(cmd.solicitanteUsuarioId(), datosZona, domainEventsFactory);

    // 4) Persistir agregado y outbox (misma transacción)
    afiliacionStore.save(agg);           // entidad JPA ↔ agregado
    var events = agg.pullDomainEvents(); // extrae y limpia el buffer interno
    outboxStore.saveAll(events);         // guarda en outbox (infra)

    // 5) Resultado para el API
    return new SolicitarAfiliacionZonaResult(
        agg.getAfiliacionId().value(),
        agg.getZonaId().value(),
        "Solicitud de afiliación registrada en estado PENDIENTE");
  }
}
