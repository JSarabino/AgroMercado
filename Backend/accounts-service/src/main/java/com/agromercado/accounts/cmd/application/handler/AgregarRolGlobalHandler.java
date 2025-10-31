package com.agromercado.accounts.cmd.application.handler;

import org.springframework.stereotype.Service;

import com.agromercado.accounts.cmd.application.command.usuario.AgregarRolGlobalCommand;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

import jakarta.transaction.Transactional;

@Service
public class AgregarRolGlobalHandler {
  private final UsuarioInterface store; private final OutboxInterface outbox; private final DomainEvents de;
  public AgregarRolGlobalHandler(UsuarioInterface s, OutboxInterface o, DomainEvents d){ this.store=s; this.outbox=o; this.de=d; }

  @Transactional
  public void handle(AgregarRolGlobalCommand cmd){
    var agg = store.findById(cmd.usuarioId()).orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));
    agg.agregarRolGlobal(cmd.rolGlobal(), de, cmd.adminActorId());
    store.save(agg);
    outbox.saveAll(agg.pullDomainEvents());
  }
}
