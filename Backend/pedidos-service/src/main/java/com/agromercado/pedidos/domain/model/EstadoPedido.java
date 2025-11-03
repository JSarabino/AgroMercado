package com.agromercado.pedidos.domain.model;

public enum EstadoPedido {
    CARRITO,           // En el carrito de compras (no confirmado)
    PENDIENTE,         // Pedido confirmado, esperando pago
    PAGADO,            // Pago confirmado
    EN_PREPARACION,    // En preparación
    ENVIADO,           // Enviado al cliente
    ENTREGADO,         // Entregado
    CANCELADO          // Cancelado
}
