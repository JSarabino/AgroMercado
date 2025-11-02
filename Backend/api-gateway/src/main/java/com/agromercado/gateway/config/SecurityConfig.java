package com.agromercado.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad para API Gateway
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Deshabilitar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())

            // Configurar autorización
            .authorizeExchange(exchanges -> exchanges
                // Permitir todas las peticiones OPTIONS (CORS preflight)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Permitir acceso público a endpoints de autenticación
                .pathMatchers("/auth/**").permitAll()

                // Permitir acceso a Actuator y Eureka
                .pathMatchers("/actuator/**", "/eureka/**").permitAll()

                // DESARROLLO: Permitir acceso a endpoints cmd y qry
                .pathMatchers("/cmd/**", "/qry/**").permitAll()

                // DESARROLLO: Permitir acceso a productos
                .pathMatchers("/productos/**").permitAll()

                // Todas las demás peticiones requieren autenticación
                .anyExchange().authenticated()
            )

            // Deshabilitar la autenticación HTTP Basic predeterminada
            .httpBasic(httpBasic -> httpBasic.disable())

            // Deshabilitar el formulario de login predeterminado
            .formLogin(formLogin -> formLogin.disable())

            .build();
    }
}
