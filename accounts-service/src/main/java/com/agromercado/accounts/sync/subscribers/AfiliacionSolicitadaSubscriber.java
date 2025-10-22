package com.agromercado.accounts.sync.subscribers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.qry.proyections.AfiliacionZonaProjector;
import com.agromercado.accounts.qry.proyections.dto.AfiliacionSolicitadaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AfiliacionSolicitadaSubscriber {

  private final ObjectMapper mapper;
  private final AfiliacionZonaProjector projector;

  public AfiliacionSolicitadaSubscriber(ObjectMapper mapper,
                                        AfiliacionZonaProjector projector) {
    this.mapper = mapper;
    this.projector = projector;
  }

  @RabbitListener(queues = "agromercado.qry.afiliaciones")
  public void onMessage(String payloadJson) {
    try {
      AfiliacionSolicitadaDTO dto = mapper.readValue(payloadJson, AfiliacionSolicitadaDTO.class);
      projector.on(dto);
    } catch (Exception e) {
      throw new RuntimeException("Error procesando AfiliacionSolicitada", e);
    }
  }
}
