package com.agromercado.accounts.cmd.infrastructure.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .addModule(new Jdk8Module())     // Optional<T>
        .addModule(new JavaTimeModule()) // Instant, LocalDate, etc.
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .build();
  }
}