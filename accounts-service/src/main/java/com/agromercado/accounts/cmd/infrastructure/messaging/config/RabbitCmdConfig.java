package com.agromercado.accounts.cmd.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitCmdConfig {

  // Cola para comandos derivados de eventos (otorgar membresía, etc.)
  public static final String CMD_QUEUE = "agromercado.cmd.usuarios";

  // Usamos el MISMO exchange de eventos (publicado por CMD y consumido por QRY/SYNC)
  public static final String EVENTS_EXCHANGE = "agromercado.events.exchange";

  @Bean("cmdExchange")
  public TopicExchange cmdExchange() {
    // durable=true, autoDelete=false
    return new TopicExchange(EVENTS_EXCHANGE, true, false);
  }

  @Bean("cmdQueue")
  public Queue cmdQueue() {
    return QueueBuilder.durable(CMD_QUEUE).build();
  }

  // Enlaza la cola CMD a la RK AfiliacionAprobada.v1 del exchange de eventos
  @Bean("cmdBindingAfiliacionAprobada")
  public Binding cmdBindingAfiliacionAprobada(
      @Qualifier("cmdExchange") TopicExchange exchange,
      @Qualifier("cmdQueue") Queue queue
  ) {
    return BindingBuilder.bind(queue)
        .to(exchange)
        .with(com.agromercado.accounts.sync.config.RabbitSyncConfig.RK_AFILIACION_APROBADA);
  }

  @Bean("cmdRabbitTemplate")
  public RabbitTemplate cmdRabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate tpl = new RabbitTemplate(connectionFactory);
    // Opcional: fijar exchange por defecto para convertAndSend(routingKey, payload)
    tpl.setExchange(EVENTS_EXCHANGE);
    return tpl;
  }
}