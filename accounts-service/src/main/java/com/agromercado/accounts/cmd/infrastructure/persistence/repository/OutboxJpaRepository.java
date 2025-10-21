package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.OutboxEntity;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, Long> {

  // Opción simple (sin locking, compila ya):
  List<OutboxEntity> findTop100ByStatusOrderByOccurredAtAsc(String status);

  // Opción robusta (locking y skip locked) — requiere Postgres:
  @Query(value = """
      SELECT * FROM outbox
      WHERE status = :status
      ORDER BY occurred_at ASC
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
      """, nativeQuery = true)
  List<OutboxEntity> lockBatchByStatusAsc(
      @Param("status") String status,
      @Param("limit")  int limit
  );
}
