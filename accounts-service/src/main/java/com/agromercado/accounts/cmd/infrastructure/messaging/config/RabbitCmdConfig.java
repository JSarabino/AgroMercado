package com.agromercado.accounts.cmd.infrastructure.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitCmdConfig {

  public static final String CMD_EVENTS_EXCHANGE = "agromercado.events.exchange";

  @Bean("cmdExchange")
  public TopicExchange cmdExchange() {
    return new TopicExchange(CMD_EVENTS_EXCHANGE, true, false);
  }

  @Bean("cmdRabbitTemplate")
  public RabbitTemplate cmdRabbitTemplate(ConnectionFactory connectionFactory) {
    return new RabbitTemplate(connectionFactory);
  }
}