package com.agromercado.accounts.cmd.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()   // <--- permitir todo
        )
        .build();
  }
}


/*
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS)) 
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/cmd/afiliaciones/solicitar").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                // habilita JWT para lo protegido; este bloque no afectará la ruta permitida
                .oauth2ResourceServer(oauth -> oauth.jwt())
                .build();
    }

}
 */
