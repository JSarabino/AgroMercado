package com.agromercado.accounts.cmd.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.api.dto.out.LoginResponse;
import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.aggregate.Usuario;

@Service
public class AuthenticationService {

  private final UsuarioInterface usuarioStore;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final long defaultTtl;

  public AuthenticationService(
      UsuarioInterface usuarioStore,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      @Value("${app.jwt.dev.ttl-seconds:3600}") long defaultTtl) {
    this.usuarioStore = usuarioStore;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.defaultTtl = defaultTtl;
  }

  /**
   * Autentica usuario por email/password y retorna LoginResponse con JWT
   */
  public LoginResponse authenticate(String email, String password) {
    // 1. Buscar usuario por email
    Usuario usuario = usuarioStore.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

    // 2. Verificar contraseña
    if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
      throw new IllegalArgumentException("Credenciales inválidas");
    }

    // 3. Verificar estado
    if (!"ACTIVO".equals(usuario.getEstado())) {
      throw new IllegalArgumentException("Usuario no activo");
    }

    // 4. Generar JWT
    String accessToken = generateJwt(usuario);

    // 5. Preparar membresías (formato: "ZONA_ID:ROL_ZONAL")
    List<String> membresias = usuario.getMembresias().values().stream()
        .filter(m -> m.estado().name().equals("ACTIVA"))
        .map(m -> m.zonaId() + ":" + m.rol().name())
        .toList();

    // 6. Retornar respuesta
    return new LoginResponse(
        accessToken,
        "Bearer",
        defaultTtl,
        usuario.getUsuarioId(),
        usuario.getEmail(),
        usuario.getNombre(),
        usuario.getTipoUsuario().name(),
        new ArrayList<>(usuario.getRolesGlobales()),
        membresias
    );
  }

  private String generateJwt(Usuario usuario) {
    var now = Instant.now();
    var roles = new ArrayList<>(usuario.getRolesGlobales());

    // Añadir roles zonales a la lista de roles
    usuario.getMembresias().values().forEach(m -> {
      if (m.estado().name().equals("ACTIVA")) {
        roles.add(m.rol().name()); // ADMIN_ZONA o PRODUCTOR
      }
    });

    // Si no hay roles, agregar el tipo_usuario como rol por defecto
    if (roles.isEmpty()) {
      roles.add(usuario.getTipoUsuario().name()); // PRODUCTOR, CLIENTE, etc.
    }

    var claims = JwtClaimsSet.builder()
        .subject(usuario.getUsuarioId())
        .issuedAt(now)
        .expiresAt(now.plusSeconds(defaultTtl))
        .claim("email", usuario.getEmail())
        .claim("nombre", usuario.getNombre())
        .claim("tipo_usuario", usuario.getTipoUsuario().name())
        .claim("roles", roles)
        .build();

    var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
    return jwtEncoder.encode(params).getTokenValue();
  }
}
