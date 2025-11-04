package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.Instant;

import com.agromercado.accounts.cmd.domain.enum_.TipoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="usuarios")
public class UsuarioEntity {
  @Id @Column(name="usuario_id") private String usuarioId;
  @Column(nullable=false, unique=true) private String email;
  @Column(nullable=false) private String nombre;
  @Column(name="password_hash") private String passwordHash;
  @Enumerated(EnumType.STRING)
  @Column(name="tipo_usuario", nullable=false) private TipoUsuario tipoUsuario;
  @Column(name="estado_usuario", nullable=false) private String estadoUsuario;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  protected UsuarioEntity(){}
  public UsuarioEntity(String id,String email,String nombre,String passwordHash,TipoUsuario tipoUsuario,String estado,Instant createdAt){
    this.usuarioId=id; this.email=email; this.nombre=nombre; this.passwordHash=passwordHash; this.tipoUsuario=tipoUsuario; this.estadoUsuario=estado; this.createdAt=createdAt;
  }
}
