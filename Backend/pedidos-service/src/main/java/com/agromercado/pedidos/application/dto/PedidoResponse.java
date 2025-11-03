package com.agromercado.pedidos.application.dto;

import com.agromercado.pedidos.domain.model.EstadoPago;
import com.agromercado.pedidos.domain.model.EstadoPedido;
import com.agromercado.pedidos.domain.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {

    private Long id;
    private String clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String zonaId;
    private String numeroPedido;
    private EstadoPedido estado;
    private EstadoPago estadoPago;
    private MetodoPago metodoPago;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String direccionEntrega;
    private String telefonoContacto;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaEntrega;
    private String transaccionPagoId;
    private List<DetallePedidoDTO> detalles;
}
