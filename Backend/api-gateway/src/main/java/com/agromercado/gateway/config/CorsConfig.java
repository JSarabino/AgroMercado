package com.agromercado.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración global de CORS para Spring Cloud Gateway (Reactivo)
 */
@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Permitir credenciales
        corsConfiguration.setAllowCredentials(true);

        // Orígenes permitidos
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Métodos HTTP permitidos
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Headers permitidos
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));

        // Headers expuestos
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-User-Id",
            "X-Tenant-Id",
            "X-User-Roles"
        ));

        // Tiempo de cache para preflight
        corsConfiguration.setMaxAge(3600L);

        // Registrar la configuración para todos los paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
