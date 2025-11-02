package com.agromercado.accounts.qry.views;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SolicitudProductorViewRepository extends MongoRepository<SolicitudProductorView, String> {

  // Consultar todas las solicitudes de una zona
  List<SolicitudProductorView> findByZonaId(String zonaId);

  // Consultar todas las solicitudes de un productor
  List<SolicitudProductorView> findByProductorUsuarioId(String productorUsuarioId);

  // Consultar solicitudes por zona y estado
  List<SolicitudProductorView> findByZonaIdAndEstado(String zonaId, String estado);

  // Consultar solicitudes por estado
  List<SolicitudProductorView> findByEstado(String estado);
}
