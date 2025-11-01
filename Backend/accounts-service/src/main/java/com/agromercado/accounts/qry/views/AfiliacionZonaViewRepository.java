package com.agromercado.accounts.qry.views;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AfiliacionZonaViewRepository extends MongoRepository<AfiliacionZonaView, String> {

  // Consultar por zona (HU03)
  List<AfiliacionZonaView> findByZonaId(String zonaId);

  // Buscar por solicitante
  List<AfiliacionZonaView> findBySolicitanteUsuarioId(String solicitanteUsuarioId);

  // Buscar por estado (para listar zonas aprobadas disponibles para productores)
  List<AfiliacionZonaView> findByEstado(String estado);
}
