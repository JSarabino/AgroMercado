package com.agromercado.accounts.sync.subscribers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.qry.proyections.AfiliacionZonaProjector;
import com.agromercado.accounts.qry.proyections.dto.AfiliacionSolicitadaDTO;
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
    final String json = message.getPayload();

    // Null-safe: si no viene el header, usa cadena vacía
    final String rk = (String) message.getHeaders()
        .getOrDefault(AmqpHeaders.RECEIVED_ROUTING_KEY, "");

    try {
      if (RabbitSyncConfig.RK_AFILIACION_SOLICITADA.equals(rk)) {
        AfiliacionSolicitadaDTO dto =
            mapper.readValue(json, AfiliacionSolicitadaDTO.class);
        projector.on(dto);
        return;
      }

      if (RabbitSyncConfig.RK_AFILIACION_APROBADA.equals(rk)) {
        var dto = mapper.readValue(json,
            com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg.class);
        projector.onAprobada(dto);
        return;
      }

      if (RabbitSyncConfig.RK_AFILIACION_RECHAZADA.equals(rk)) {
        var dto = mapper.readValue(json,
            com.agromercado.accounts.sync.contracts.AfiliacionRechazadaMsg.class);
        projector.onRechazada(dto);
        return;
      }

      // rk vacío o no reconocido
      throw new IllegalArgumentException("Routing key no soportada o vacía: '" + rk + "'");
    } catch (Exception e) {
      throw new RuntimeException("Error procesando mensaje (" + rk + "): " + e.getMessage(), e);
    }
  }
}