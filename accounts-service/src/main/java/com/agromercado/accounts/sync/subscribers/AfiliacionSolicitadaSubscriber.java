package com.agromercado.accounts.sync.subscribers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.qry.proyections.AfiliacionZonaProjector;
import com.agromercado.accounts.sync.config.RabbitSyncConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AfiliacionSolicitadaSubscriber {

  private final ObjectMapper mapper;
  private final AfiliacionZonaProjector projector;

  public AfiliacionSolicitadaSubscriber(ObjectMapper mapper, AfiliacionZonaProjector projector) {
    this.mapper = mapper;
    this.projector = projector;
  }

  @RabbitListener(queues = RabbitSyncConfig.SYNC_QUEUE)
  public void onMessage(Message<String> message) {
    String json = message.getPayload();
    String rk   = (String) message.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY);

    try {
      switch (rk) {
        case RabbitSyncConfig.RK_AFILIACION_SOLICITADA -> {
          var dto = mapper.readValue(json, com.agromercado.accounts.qry.proyections.dto.AfiliacionSolicitadaDTO.class);
          projector.on(dto);
        }
        case RabbitSyncConfig.RK_AFILIACION_APROBADA -> {
          var dto = mapper.readValue(json, com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg.class);
          projector.onAprobada(dto);
        }
        case RabbitSyncConfig.RK_AFILIACION_RECHAZADA -> {
          var dto = mapper.readValue(json, com.agromercado.accounts.sync.contracts.AfiliacionRechazadaMsg.class);
          projector.onRechazada(dto);
        }
        default -> throw new IllegalArgumentException("Routing key no soportada: " + rk);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error procesando mensaje (" + rk + "): " + e.getMessage(), e);
    }
  }
}