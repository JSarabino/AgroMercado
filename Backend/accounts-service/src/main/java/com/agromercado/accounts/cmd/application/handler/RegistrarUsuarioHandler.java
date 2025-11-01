package com.agromercado.accounts.cmd.application.handler;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.application.command.RegistrarUsuarioCommand;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.aggregate.Usuario;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

import jakarta.transaction.Transactional;

@Service
public class RegistrarUsuarioHandler {
  private final UsuarioInterface store;
  private final OutboxInterface outbox;
  private final DomainEvents de;
  private final PasswordEncoder passwordEncoder;

  public RegistrarUsuarioHandler(UsuarioInterface s, OutboxInterface o, DomainEvents d, PasswordEncoder p){
    this.store=s; this.outbox=o; this.de=d; this.passwordEncoder=p;
  }

  @Transactional
  public String handle(RegistrarUsuarioCommand cmd){
    if (store.existsByEmail(cmd.email())) throw new IllegalArgumentException("Email ya existe");
    String passwordHash = passwordEncoder.encode(cmd.password());
    var agg = Usuario.registrar(cmd.email(), cmd.nombre(), passwordHash, cmd.tipoUsuario(), de);
    store.save(agg);
    outbox.saveAll(agg.pullDomainEvents());
    return agg.getUsuarioId();
  }
}
