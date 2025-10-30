package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="usuarios_roles_globales")
@IdClass(UsuarioRolGlobalEntity.PK.class)
public class UsuarioRolGlobalEntity {
  @Id @Column(name="usuario_id") private String usuarioId;
  @Id @Column(name="rol_global") private String rolGlobal;
  @Column(name="granted_at", nullable=false) private Instant grantedAt;

  public static class PK implements Serializable {
    public String usuarioId; public String rolGlobal;
    @Override public boolean equals(Object o){/*...*/ return Objects.equals(((PK)o).usuarioId,usuarioId)&&Objects.equals(((PK)o).rolGlobal,rolGlobal);}
    @Override public int hashCode(){ return Objects.hash(usuarioId,rolGlobal); }
  }
  protected UsuarioRolGlobalEntity(){}
  public UsuarioRolGlobalEntity(String usuarioId,String rolGlobal,Instant grantedAt){
    this.usuarioId=usuarioId; this.rolGlobal=rolGlobal; this.grantedAt=grantedAt;
  }
}