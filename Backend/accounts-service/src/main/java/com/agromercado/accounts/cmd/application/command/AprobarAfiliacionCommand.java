package com.agromercado.accounts.cmd.application.command;

public record AprobarAfiliacionCommand(
  String afiliacionId,
  String adminGlobalId,
  String observaciones
) {}
