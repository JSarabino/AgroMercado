package com.agromercado.accounts.qry.proyections;

public class ProjectionException extends RuntimeException {
  public ProjectionException(String message) { super(message); }
  public ProjectionException(String message, Throwable cause) { super(message, cause); }
}