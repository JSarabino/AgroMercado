package com.agromercado.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.jwt.dev.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Rutas públicas
                .pathMatchers(HttpMethod.POST, "/auth/dev/token").permitAll()
                .pathMatchers(HttpMethod.POST, "/cmd/usuarios").permitAll()
                .pathMatchers(HttpMethod.POST, "/accounts/cmd/usuarios").permitAll()
                .pathMatchers(HttpMethod.POST, "/accounts/auth/dev/token").permitAll()

                // Actuator y Eureka
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/eureka/**").permitAll()

                // Productos - requiere autenticación
                .pathMatchers("/productos/**").authenticated()
                .pathMatchers("/api/productos/**").authenticated()

                // Todo lo demás requiere autenticación
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusReactiveJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }
}
