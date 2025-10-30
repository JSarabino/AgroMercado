package com.agromercado.accounts.cmd.application.command.usuario;

import com.agromercado.accounts.cmd.domain.enum_.RolZonal;

public record OtorgarMembresiaZonalCommand(String usuarioId, String zonaId, RolZonal rolZonal, String causedBy) {}
