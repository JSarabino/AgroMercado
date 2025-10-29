package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios_roles_globales")
@IdClass(UsuarioRolGlobalEntity.PK.class)
public class UsuarioRolGlobalEntity {
  @Id @Column(name = "usuario_id", length = 50)
  private String usuarioId;

  @Id @Column(name = "rol_global", length = 60)
  private String rolGlobal;

  @Column(name = "granted_at", nullable = false)
  private OffsetDateTime grantedAt = OffsetDateTime.now();

  public static class PK implements Serializable {
    private String usuarioId;
    private String rolGlobal;
    // equals/hashCode
  }
  // getters/setters
}