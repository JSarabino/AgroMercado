package com.agromercado.accounts.cmd.api.controller;

import java.time.Instant;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/dev")
public class AuthDevController {

  private final JwtEncoder encoder;
  private final long defaultTtl;

  public AuthDevController(JwtEncoder encoder,
                           @Value("${app.jwt.dev.ttl-seconds:3600}") long defaultTtl) {
    this.encoder = encoder;
    this.defaultTtl = defaultTtl;
  }

  public record DevTokenRequest(String userId, List<String> roles, Long ttlSeconds) {}

  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> issue(@RequestBody DevTokenRequest req) {
    var now = Instant.now();
    var ttl = (req.ttlSeconds() != null && req.ttlSeconds() > 0) ? req.ttlSeconds() : defaultTtl;

    var claims = JwtClaimsSet.builder()
        .subject(req.userId())
        .issuedAt(now)
        .expiresAt(now.plusSeconds(ttl))
        .claim("roles", Optional.ofNullable(req.roles()).orElseGet(List::of))
        .build();

    var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
    var jwt = encoder.encode(params).getTokenValue();

    return Map.of("access_token", jwt, "token_type", "Bearer", "expires_in", ttl);
  }
}
