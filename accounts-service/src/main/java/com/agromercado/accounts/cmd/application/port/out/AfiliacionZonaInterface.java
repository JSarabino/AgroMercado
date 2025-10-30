package com.agromercado.accounts.cmd.application.port.out;

import java.util.Optional;

import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;

/** Puerto de persistencia del agregado (implementación en infrastructure). */
public interface AfiliacionZonaInterface {
  void save(AfiliacionZona aggregate);
  Optional<AfiliacionZona> findById(String afiliacionId); 
}
