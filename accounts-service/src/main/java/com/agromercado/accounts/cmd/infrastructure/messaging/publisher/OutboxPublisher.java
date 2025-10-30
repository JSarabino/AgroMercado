package com.agromercado.accounts.cmd.infrastructure.messaging.publisher;

import java.time.Instant;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.OutboxJpaRepository;

@Component
public class OutboxPublisher {

  private final OutboxJpaRepository repo;
  private final RabbitTemplate rabbit;

  public OutboxPublisher(
      OutboxJpaRepository repo,
      @Qualifier("cmdRabbitTemplate") RabbitTemplate rabbitTemplate
  ) {
    this.repo = repo;
    this.rabbit = rabbitTemplate;
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEntity> pendientes = repo.findTop100ByStatusOrderByOccurredAtAsc("PENDING");

    for (OutboxEntity e : pendientes) {
      try {
        String routingKey = ensureVersionedRoutingKey(e.getEventType());
        // exchange ya está seteado en el template (RabbitCmdConfig.cmdRabbitTemplate)
        rabbit.convertAndSend(routingKey, e.getPayload());

        e.setStatus("SENT");
        e.setSentAt(Instant.now());
        e.setLastError(null);
      } catch (Exception ex) {
        e.setStatus("FAILED");
        e.setLastError(shorten(ex.getMessage(), 500));
      }
    }
    // Dentro de @Transactional: flush/commit automático
  }

  /** Si el eventType ya viene con ".vX", úsalo tal cual; si no, agrega ".v1". */
  private static String ensureVersionedRoutingKey(String eventType) {
    if (eventType == null || eventType.isBlank()) return "Unknown.v1";
    // Heurística simple: si contiene ".v" seguido de dígitos, se asume versionado
    if (eventType.matches(".*\\.v\\d+$")) return eventType;
    return eventType + ".v1";
  }

  private static String shorten(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }
}