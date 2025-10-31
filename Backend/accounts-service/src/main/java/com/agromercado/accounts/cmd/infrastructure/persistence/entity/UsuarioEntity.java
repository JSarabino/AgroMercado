package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.time.Instant;

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
  @Column(name="estado_usuario", nullable=false) private String estadoUsuario;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  protected UsuarioEntity(){}
  public UsuarioEntity(String id,String email,String nombre,String estado,Instant createdAt){
    this.usuarioId=id; this.email=email; this.nombre=nombre; this.estadoUsuario=estado; this.createdAt=createdAt;
  }
}
