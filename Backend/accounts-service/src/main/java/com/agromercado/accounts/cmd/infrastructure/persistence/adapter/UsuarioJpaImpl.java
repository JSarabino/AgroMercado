package com.agromercado.accounts.cmd.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.aggregate.Usuario;
import com.agromercado.accounts.cmd.infrastructure.persistence.mapper.UsuarioMapper;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioJpaRepository;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioMembresiaJpaRepository;
import com.agromercado.accounts.cmd.infrastructure.persistence.repository.UsuarioRolGlobalJpaRepository;

@Component
public class UsuarioJpaImpl implements UsuarioInterface {
  private final UsuarioJpaRepository userRepo;
  private final UsuarioRolGlobalJpaRepository rolRepo;
  private final UsuarioMembresiaJpaRepository membRepo;

  public UsuarioJpaImpl(UsuarioJpaRepository u, UsuarioRolGlobalJpaRepository r, UsuarioMembresiaJpaRepository m){
    this.userRepo=u; this.rolRepo=r; this.membRepo=m;
  }

  @Override
  public Optional<Usuario> findById(String id){
    return userRepo.findById(id).map(u ->
      UsuarioMapper.toAggregate(u, rolRepo.findByUsuarioId(id), membRepo.findByUsuarioId(id))
    );
  }

  @Override public boolean existsByEmail(String email){ return userRepo.existsByEmail(email); }

  @Override @Transactional
  public void save(Usuario agg){
    userRepo.save(UsuarioMapper.toUsuarioEntity(agg));
    // simplificado MVP: upsert “apend-only”
    UsuarioMapper.toRolGlobalEntities(agg).forEach(rolRepo::save);
    UsuarioMapper.toMembresias(agg).forEach(membRepo::save);
  }
}
