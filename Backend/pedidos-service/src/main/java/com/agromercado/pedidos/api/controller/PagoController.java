package com.agromercado.pedidos.api.controller;

import com.agromercado.pedidos.application.dto.PagoResponse;
import com.agromercado.pedidos.application.dto.ProcesarPagoRequest;
import com.agromercado.pedidos.application.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para procesamiento de pagos
 */
@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PedidoService pedidoService;

    /**
     * POST /pagos/procesar - Procesa el pago de un pedido
     */
    @PostMapping("/procesar")
    public ResponseEntity<PagoResponse> procesarPago(
            @RequestHeader("X-User-Id") String clienteId,
            @Valid @RequestBody ProcesarPagoRequest request) {

        PagoResponse response = pedidoService.procesarPago(clienteId, request);
        return ResponseEntity.ok(response);
    }
}
