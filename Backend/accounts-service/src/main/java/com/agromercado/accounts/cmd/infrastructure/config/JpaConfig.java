package com.agromercado.accounts.cmd.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.agromercado.accounts.cmd.infrastructure.persistence.repository")
public class JpaConfig {}
