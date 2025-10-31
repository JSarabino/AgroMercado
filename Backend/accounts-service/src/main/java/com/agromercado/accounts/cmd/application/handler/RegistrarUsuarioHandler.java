package com.agromercado.accounts.cmd.application.handler;

import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.application.command.RegistrarUsuarioCommand;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.aggregate.Usuario;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

import jakarta.transaction.Transactional;

@Service
public class RegistrarUsuarioHandler {
  private final UsuarioInterface store; private final OutboxInterface outbox; private final DomainEvents de;
  public RegistrarUsuarioHandler(UsuarioInterface s, OutboxInterface o, DomainEvents d){ this.store=s; this.outbox=o; this.de=d; }

  @Transactional
  public String handle(RegistrarUsuarioCommand cmd){
    if (store.existsByEmail(cmd.email())) throw new IllegalArgumentException("Email ya existe");
    var agg = Usuario.registrar(cmd.email(), cmd.nombre(), de);
    store.save(agg);
    outbox.saveAll(agg.pullDomainEvents());
    return agg.getUsuarioId();
  }
}
