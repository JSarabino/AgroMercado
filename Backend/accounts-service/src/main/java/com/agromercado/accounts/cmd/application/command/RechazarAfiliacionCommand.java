package com.agromercado.accounts.cmd.application.command;

public record RechazarAfiliacionCommand(
  String afiliacionId,
  String adminGlobalId,
  String observaciones
) {}
