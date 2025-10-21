package com.agromercado.accounts.cmd.infrastructure.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  public static final String EVENTS_EXCHANGE = "agromercado.events.exchange";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE, true, false);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    return new RabbitTemplate(connectionFactory);
  }
}
