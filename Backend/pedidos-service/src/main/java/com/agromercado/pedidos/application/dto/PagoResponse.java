package com.agromercado.pedidos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {

    private String transaccionId;
    private boolean aprobado;
    private String mensaje;
    private BigDecimal monto;
    private String metodoPago;
    private Long pedidoId;
    private String numeroPedido;
}
