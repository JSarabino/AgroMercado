package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "outbox")
public class OutboxEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, unique = true)
  private String eventId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
  private String payload;

  @Column(name = "aggregate_id")
  private String aggregateId;

  @Column(name = "zona_id")
  private String zonaId;

  @Column(name = "status", length = 20, nullable = false)
  private String status; // PENDING / SENT / FAILED

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "last_error", length = 500)
  private String lastError;

  protected OutboxEntity() {}

  public OutboxEntity(String eventId, String eventType, String payload, String aggregateId,
                      String zonaId, String status, Instant occurredAt, Instant createdAt) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.payload = payload;
    this.aggregateId = aggregateId;
    this.zonaId = zonaId;
    this.status = status;
    this.occurredAt = occurredAt;
    this.createdAt = createdAt;
  }

  // GETTERS/SETTERS necesarios para el publisher
  public Long getId() { return id; }
  public String getEventId() { return eventId; }
  public String getEventType() { return eventType; }
  public String getPayload() { return payload; }
  public String getAggregateId() { return aggregateId; }
  public String getZonaId() { return zonaId; }
  public String getStatus() { return status; }
  public Instant getOccurredAt() { return occurredAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getSentAt() { return sentAt; }
  public String getLastError() { return lastError; }

  public void setStatus(String status) { this.status = status; }
  public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
  public void setLastError(String lastError) { this.lastError = lastError; }
}
