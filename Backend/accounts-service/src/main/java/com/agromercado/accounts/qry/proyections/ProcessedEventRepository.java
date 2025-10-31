package com.agromercado.accounts.qry.proyections;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {
  Optional<ProcessedEvent> findByEventId(String eventId);
}
