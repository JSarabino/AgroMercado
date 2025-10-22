package com.agromercado.accounts.sync.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitSyncConfig {

  public static final String SYNC_EXCHANGE = "agromercado.events.exchange";
  public static final String SYNC_QUEUE = "agromercado.qry.afiliaciones";
  public static final String RK_AFILIACION_SOLICITADA = "AfiliacionSolicitada.v1";

  @Bean("syncExchange")
  public TopicExchange syncExchange() {
    return new TopicExchange(SYNC_EXCHANGE, true, false);
  }

  @Bean("syncQueue")
  public Queue syncQueue() {
    return QueueBuilder.durable(SYNC_QUEUE).build();
  }

  @Bean("syncBinding")
  public Binding syncBinding(@Qualifier("syncExchange") TopicExchange exchange,
                             @Qualifier("syncQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(exchange).with(RK_AFILIACION_SOLICITADA);
  }

  @Bean("syncRabbitTemplate")
  public RabbitTemplate syncRabbitTemplate(ConnectionFactory cf) {
    return new RabbitTemplate(cf);
  }
}
