package com.agromercado.pedidos.application.dto;

import com.agromercado.pedidos.domain.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarPagoRequest {

    @NotNull(message = "El ID del pedido es requerido")
    private Long pedidoId;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    // Información de tarjeta (simulada - no se almacena)
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento;
    private String cvv;
}
