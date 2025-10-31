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

  // Routing keys
  public static final String RK_AFILIACION_SOLICITADA = "AfiliacionSolicitada.v1";
  public static final String RK_AFILIACION_APROBADA   = "AfiliacionAprobada.v1";
  public static final String RK_AFILIACION_RECHAZADA  = "AfiliacionRechazada.v1";

  @Bean("syncExchange")
  public TopicExchange syncExchange() {
    return new TopicExchange(SYNC_EXCHANGE, true, false);
  }

  @Bean("syncQueue")
  public Queue syncQueue() {
    return QueueBuilder.durable(SYNC_QUEUE).build();
  }

  // Binding existente
  @Bean("syncBindingSolicitada")
  public Binding syncBindingSolicitada(@Qualifier("syncExchange") TopicExchange exchange,
                                       @Qualifier("syncQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(exchange).with(RK_AFILIACION_SOLICITADA);
  }

  // NUEVOS bindings
  @Bean("syncBindingAprobada")
  public Binding syncBindingAprobada(@Qualifier("syncExchange") TopicExchange exchange,
                                     @Qualifier("syncQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(exchange).with(RK_AFILIACION_APROBADA);
  }

  @Bean("syncBindingRechazada")
  public Binding syncBindingRechazada(@Qualifier("syncExchange") TopicExchange exchange,
                                      @Qualifier("syncQueue") Queue queue) {
    return BindingBuilder.bind(queue).to(exchange).with(RK_AFILIACION_RECHAZADA);
  }

  @Bean("syncRabbitTemplate")
  public RabbitTemplate syncRabbitTemplate(ConnectionFactory cf) {
    return new RabbitTemplate(cf);
  }
}
