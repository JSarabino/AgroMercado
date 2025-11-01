package com.agromercado.accounts.cmd.infrastructure.messaging.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.application.command.usuario.OtorgarMembresiaZonalCommand;
import com.agromercado.accounts.cmd.application.handler.OtorgarMembresiaZonalHandler;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;
import com.agromercado.accounts.cmd.infrastructure.messaging.config.RabbitCmdConfig;
import com.agromercado.accounts.sync.contracts.AfiliacionAprobadaMsg;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AfiliacionAprobadaCmdSubscriber {

  private static final Logger log = LoggerFactory.getLogger(AfiliacionAprobadaCmdSubscriber.class);

  private final ObjectMapper mapper;
  private final OtorgarMembresiaZonalHandler handler;

  public AfiliacionAprobadaCmdSubscriber(ObjectMapper mapper, OtorgarMembresiaZonalHandler handler){
    this.mapper = mapper;
    this.handler = handler;
  }

  @RabbitListener(queues = RabbitCmdConfig.CMD_QUEUE)
  public void onMessage(Message<String> message){
    final String json = message.getPayload();
    final String rk = (String) message.getHeaders()
        .getOrDefault(AmqpHeaders.RECEIVED_ROUTING_KEY,
                      ""); // null-safe

    if (!com.agromercado.accounts.sync.config.RabbitSyncConfig.RK_AFILIACION_APROBADA.equals(rk)) {
      // Ignora otros mensajes que eventualmente lleguen a la cola
      return;
    }

    try {
      AfiliacionAprobadaMsg evt = mapper.readValue(json, AfiliacionAprobadaMsg.class);
      handler.handle(new OtorgarMembresiaZonalCommand(
          evt.solicitanteUsuarioId(),
          evt.zonaId(),
          RolZonal.ADMIN_ZONA,
          evt.meta().causedByUserId()
      ));
      log.debug("Membresía zonal otorgada exitosamente para usuario {} en zona {}",
                evt.solicitanteUsuarioId(), evt.zonaId());
    } catch (IllegalArgumentException ex) {
      // Usuario no existe - probablemente un mensaje viejo/huérfano
      log.warn("No se pudo otorgar membresía: {}. Usuario={}, Zona={}",
               ex.getMessage(),
               extractUsuarioId(json),
               extractZonaId(json));
      // NO relanza la excepción - el mensaje se considera procesado
    } catch (Exception ex) {
      log.error("Error crítico procesando AfiliacionAprobada: {}", json, ex);
      throw new RuntimeException("Error otorgando membresía por AfiliacionAprobada", ex);
    }
  }

  private String extractUsuarioId(String json) {
    try {
      return mapper.readTree(json).path("solicitanteUsuarioId").asText("unknown");
    } catch (Exception e) {
      return "unknown";
    }
  }

  private String extractZonaId(String json) {
    try {
      return mapper.readTree(json).path("zonaId").asText("unknown");
    } catch (Exception e) {
      return "unknown";
    }
  }
}