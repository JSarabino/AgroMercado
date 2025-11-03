package com.agromercado.pedidos.application.dto;

import com.agromercado.pedidos.domain.model.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarPedidoRequest {

    @NotBlank(message = "La dirección de entrega es requerida")
    private String direccionEntrega;

    @NotBlank(message = "El teléfono de contacto es requerido")
    private String telefonoContacto;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    private String notas;
}
