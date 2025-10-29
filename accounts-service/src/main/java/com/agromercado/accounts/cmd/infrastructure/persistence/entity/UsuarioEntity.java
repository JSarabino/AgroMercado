package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name = "usuarios")
public class UsuarioEntity {
  @Id
  @Column(name = "usuario_id", length = 50)
  private String usuarioId;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(name = "estado_usuario", nullable = false, length = 30)
  private String estadoUsuario = "ACTIVO";

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  // getters/setters/constructores vacíos
}
