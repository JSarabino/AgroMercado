package com.agromercado.accounts.cmd.infrastructure.messaging.publisher;

import java.time.Instant;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.OutboxJpaRepository;

@Component
public class OutboxPublisher {

  private final OutboxJpaRepository repo;
  private final RabbitTemplate rabbit;
  private final String exchangeName = "agromercado.events.exchange";

  public OutboxPublisher(OutboxJpaRepository repo, RabbitTemplate rabbitTemplate) {
    this.repo = repo;
    this.rabbit = rabbitTemplate;
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void publishPendingEvents() {
    // LEE UNA LISTA TIPADA
    List<OutboxEntity> pendientes = repo.findTop100ByStatusOrderByOccurredAtAsc("PENDING");
    // Si usas la versión con locking:
    // List<OutboxEntity> pendientes = repo.lockBatchByStatusAsc("PENDING", 100);

    for (OutboxEntity e : pendientes) {
      try {
        String routingKey = e.getEventType() + ".v1";  // ej: afiliacion.solicitada.v1
        rabbit.convertAndSend(exchangeName, routingKey, e.getPayload());
        e.setStatus("SENT");
        e.setSentAt(Instant.now());
        e.setLastError(null);
      } catch (Exception ex) {
        e.setStatus("FAILED");
        e.setLastError(shorten(ex.getMessage(), 500));
      }
    }
    // @Transactional hará commit de los estados SENT/FAILED
  }

  private static String shorten(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }
}
