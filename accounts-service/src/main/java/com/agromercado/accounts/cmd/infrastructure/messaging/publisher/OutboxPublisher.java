package com.agromercado.accounts.cmd.infrastructure.messaging.publisher;

import java.time.Instant;
import java.util.List;

import org.springframework.amqp.core.TopicExchange;
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
  private final TopicExchange exchange;

  public OutboxPublisher(
      OutboxJpaRepository repo,
      @Qualifier("cmdRabbitTemplate") RabbitTemplate rabbitTemplate,
      @Qualifier("cmdExchange") TopicExchange exchange
  ) {
    this.repo = repo;
    this.rabbit = rabbitTemplate;
    this.exchange = exchange;
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEntity> pendientes = repo.findTop100ByStatusOrderByOccurredAtAsc("PENDING");

    for (OutboxEntity e : pendientes) {
      try {
        String routingKey = e.getEventType() + ".v1"; // p.ej. AfiliacionSolicitada.v1
        rabbit.convertAndSend(exchange.getName(), routingKey, e.getPayload());
        e.setStatus("SENT");
        e.setSentAt(Instant.now());
        e.setLastError(null);
      } catch (Exception ex) {
        e.setStatus("FAILED");
        e.setLastError(shorten(ex.getMessage(), 500));
      }
    }
    // Al estar dentro de @Transactional, los cambios se hacen flush/commit.
  }

  private static String shorten(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }
}