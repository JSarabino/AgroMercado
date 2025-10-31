package com.agromercado.accounts.cmd.infrastructure.messaging.subscriber;

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
    } catch (Exception ex) {
      throw new RuntimeException("Error otorgando membresía por AfiliacionAprobada", ex);
    }
  }
}