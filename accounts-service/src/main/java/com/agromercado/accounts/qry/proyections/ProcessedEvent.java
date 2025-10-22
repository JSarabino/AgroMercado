package com.agromercado.accounts.qry.proyections;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Marca eventos ya procesados para asegurar idempotencia.
 */
@Document("processed_events")
public class ProcessedEvent {

  @Id
  private String eventId;

  private String aggregateId;
  private String eventType;
  private Instant processedAt;

  public ProcessedEvent() {}

  public ProcessedEvent(String eventId, String aggregateId, String eventType, Instant processedAt) {
    this.eventId = eventId;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.processedAt = processedAt;
  }

  public String getEventId() { return eventId; }
  public String getAggregateId() { return aggregateId; }
  public String getEventType() { return eventType; }
  public Instant getProcessedAt() { return processedAt; }

  public void setEventId(String eventId) { this.eventId = eventId; }
  public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
  public void setEventType(String eventType) { this.eventType = eventType; }
  public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
