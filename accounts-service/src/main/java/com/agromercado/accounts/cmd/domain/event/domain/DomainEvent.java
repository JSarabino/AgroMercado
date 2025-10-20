package com.agromercado.accounts.cmd.domain.event.domain;

import java.time.Instant;
import java.util.UUID;

/*Esta interfaz del dominio define el contrato mínimo que debe cumplir cualquier evento de dominio en tu sistema. 
Sirve para que el resto de capas (application/infra) puedan tratar todos los eventos de forma uniforme.*/

public interface DomainEvent {

    String eventType(); // Útil para ruteo y versionado (afiliacion.solicitada.v1 en el bus).
    Instant occurredAt(); // Instante exacto en que “ocurrió” el hecho en el dominio (no cuando se publica). Facilita auditoría y orden lógico.
    UUID eventId(); // UUID único del evento. Permite idempotencia en outbox/consumidores.
    EventMetadata meta(); // No es negocio, es metainformación para tracking y depuración.
}
