package com.agromercado.accounts.cmd.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agromercado.accounts.cmd.infrastructure.persistence.entity.SolicitudAfiliacionProductorEntity;

@Repository
public interface SolicitudAfiliacionProductorJpaRepository extends JpaRepository<SolicitudAfiliacionProductorEntity, String> {

  // Buscar todas las solicitudes de una zona específica
  List<SolicitudAfiliacionProductorEntity> findByZonaId(String zonaId);

  // Buscar todas las solicitudes de un productor específico
  List<SolicitudAfiliacionProductorEntity> findByProductorUsuarioId(String productorUsuarioId);

  // Buscar solicitudes por zona y estado
  List<SolicitudAfiliacionProductorEntity> findByZonaIdAndEstado(String zonaId, String estado);

  // Verificar si existe una solicitud pendiente del mismo productor a la misma zona
  Optional<SolicitudAfiliacionProductorEntity> findByProductorUsuarioIdAndZonaIdAndEstado(
      String productorUsuarioId,
      String zonaId,
      String estado
  );
}
