package com.agromercado.accounts.cmd.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class JacksonDiag {
  private final ObjectMapper mapper;
  JacksonDiag(ObjectMapper mapper) { this.mapper = mapper; }

  @EventListener(ApplicationReadyEvent.class)
  void log() {
    System.out.println("ObjectMapper = " + mapper.getClass());
    System.out.println("Jackson version = " + mapper.version());
  }
}
