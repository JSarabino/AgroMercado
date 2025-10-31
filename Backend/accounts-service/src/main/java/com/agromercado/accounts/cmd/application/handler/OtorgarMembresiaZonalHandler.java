package com.agromercado.accounts.cmd.application.handler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.accounts.cmd.application.command.usuario.OtorgarMembresiaZonalCommand;
import com.agromercado.accounts.cmd.application.port.out.OutboxInterface;
import com.agromercado.accounts.cmd.application.port.out.UsuarioInterface;
import com.agromercado.accounts.cmd.domain.event.domain.DomainEvents;

@Service
public class OtorgarMembresiaZonalHandler {
  private final UsuarioInterface store; private final OutboxInterface outbox; private final DomainEvents de;
  public OtorgarMembresiaZonalHandler(UsuarioInterface s, OutboxInterface o, DomainEvents d){ this.store=s; this.outbox=o; this.de=d; }

  @Transactional
  public void handle(OtorgarMembresiaZonalCommand cmd){
    var agg = store.findById(cmd.usuarioId()).orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));
    agg.otorgarMembresiaZonal(cmd.zonaId(), cmd.rolZonal(), de, cmd.causedBy());
    store.save(agg);
    outbox.saveAll(agg.pullDomainEvents());
  }
}