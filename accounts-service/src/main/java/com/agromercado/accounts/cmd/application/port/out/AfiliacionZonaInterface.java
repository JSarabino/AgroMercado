package com.agromercado.accounts.cmd.application.port.out;

import com.agromercado.accounts.cmd.domain.aggregate.AfiliacionZona;

/** Puerto de persistencia del agregado (implementación en infrastructure). */
public interface AfiliacionZonaInterface {
  void save(AfiliacionZona aggregate);
}
