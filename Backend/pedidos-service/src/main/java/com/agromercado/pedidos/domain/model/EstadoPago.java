package com.agromercado.pedidos.domain.model;

public enum EstadoPago {
    PENDIENTE,        // Esperando pago
    PROCESANDO,       // Procesando pago
    APROBADO,         // Pago aprobado
    RECHAZADO,        // Pago rechazado
    REEMBOLSADO       // Pago reembolsado
}
