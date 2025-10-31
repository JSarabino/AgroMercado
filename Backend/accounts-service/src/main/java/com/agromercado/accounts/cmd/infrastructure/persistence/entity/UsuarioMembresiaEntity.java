package com.agromercado.accounts.cmd.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import com.agromercado.accounts.cmd.domain.enum_.EstadoMembresia;
import com.agromercado.accounts.cmd.domain.enum_.RolZonal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="usuarios_membresias")
@IdClass(UsuarioMembresiaEntity.PK.class)
public class UsuarioMembresiaEntity {
  @Id @Column(name="usuario_id") private String usuarioId;
  @Id @Column(name="zona_id")    private String zonaId;
  @Id @Enumerated(EnumType.STRING) @Column(name="rol_zonal")  private RolZonal rolZonal;
  @Id @Enumerated(EnumType.STRING) @Column(name="estado")     private EstadoMembresia estado;
  @Column(name="created_at", nullable=false) private Instant createdAt;

  public static class PK implements Serializable {
    public String usuarioId, zonaId;
    public RolZonal rolZonal;
    public EstadoMembresia estado;
    @Override public boolean equals(Object o){/*...*/ return Objects.equals(((PK)o).usuarioId,usuarioId)&&Objects.equals(((PK)o).zonaId,zonaId)&&((PK)o).rolZonal==rolZonal&&((PK)o).estado==estado;}
    @Override public int hashCode(){ return Objects.hash(usuarioId,zonaId,rolZonal,estado); }
  }
  protected UsuarioMembresiaEntity(){}
  public UsuarioMembresiaEntity(String u,String z,RolZonal r,EstadoMembresia e,Instant at){
    this.usuarioId=u; this.zonaId=z; this.rolZonal=r; this.estado=e; this.createdAt=at;
  }
}
