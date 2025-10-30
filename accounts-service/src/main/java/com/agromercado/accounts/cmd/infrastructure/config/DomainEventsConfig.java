package com.agromercado.accounts.cmd.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

@Configuration
public class DomainEventsConfig {
  @Bean
  public DomainEvents domainEvents(Clock utcClock) {
    return new DomainEvents(utcClock);
  }
}
