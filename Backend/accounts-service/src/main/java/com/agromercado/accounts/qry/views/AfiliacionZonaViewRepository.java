package com.agromercado.accounts.qry.views;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AfiliacionZonaViewRepository extends MongoRepository<AfiliacionZonaView, String> {

  // Consultar por zona (HU03)
  List<AfiliacionZonaView> findByZonaId(String zonaId);

  // Opcional: buscar una específica
  List<AfiliacionZonaView> findBySolicitanteUsuarioId(String solicitanteUsuarioId);
}
