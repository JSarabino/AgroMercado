package com.agromercado.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Filtro global para extraer información del JWT y agregarla como headers
 * Este filtro se ejecuta para todas las solicitudes que pasan por el Gateway
 */
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.dev.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Obtener el header Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);

                // Parsear el token JWT (usar mismo encoding que accounts-service)
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                        .parseClaimsJws(token)
                        .getBody();

                    // Extraer información del token
                    String userId = claims.getSubject();
                    String userName = claims.get("nombre", String.class); // El JWT usa "nombre" no "name"
                    String userEmail = claims.get("email", String.class);

                    // Si el nombre está vacío o null, usar el userId como fallback
                    if (userName == null || userName.trim().isEmpty()) {
                        userName = userId != null ? userId : "Usuario";
                    }

                    // Roles puede ser un array, convertir a String
                    String userRoles = "";
                    try {
                        Object rolesObj = claims.get("roles");
                        if (rolesObj instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<String> rolesList = (java.util.List<String>) rolesObj;
                            userRoles = String.join(",", rolesList);
                        } else if (rolesObj instanceof String) {
                            userRoles = (String) rolesObj;
                        }
                    } catch (Exception e) {
                        System.out.println("Error al procesar roles: " + e.getMessage());
                    }

                    String tenantId = claims.get("tenantId", String.class);

                // Crear una nueva request con los headers adicionales
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Name", userName != null ? userName : "Usuario")
                        .header("X-User-Email", userEmail != null ? userEmail : "")
                        .header("X-User-Roles", userRoles != null ? userRoles : "")
                        .header("X-Tenant-Id", tenantId != null ? tenantId : "default")
                        .build();

                // Continuar con la request modificada
                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                System.out.println("JWT procesado - Usuario: " + userId + ", Roles: " + userRoles);

                return chain.filter(mutatedExchange);

            } catch (Exception e) {
                System.out.println("Error al procesar JWT: " + e.getMessage());
                // Si hay error, continuar sin modificar los headers
            }
        }

        // Si no hay token o hubo error, agregar headers por defecto para desarrollo
        ServerHttpRequest defaultRequest = request.mutate()
                .header("X-User-Id", "dev-user")
                .header("X-User-Name", "Usuario Demo")
                .header("X-User-Email", "")
                .header("X-User-Roles", "")
                .header("X-Tenant-Id", "default")
                .build();

        ServerWebExchange defaultExchange = exchange.mutate().request(defaultRequest).build();
        return chain.filter(defaultExchange);
    }

    @Override
    public int getOrder() {
        // Ejecutar este filtro con alta prioridad (números más bajos se ejecutan primero)
        return -100;
    }
}
