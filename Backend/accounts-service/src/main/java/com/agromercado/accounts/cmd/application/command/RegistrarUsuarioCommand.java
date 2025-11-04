package com.agromercado.accounts.cmd.application.command;

import com.agromercado.accounts.cmd.domain.enum_.TipoUsuario;

public record RegistrarUsuarioCommand(String email, String nombre, String password, TipoUsuario tipoUsuario) {}
