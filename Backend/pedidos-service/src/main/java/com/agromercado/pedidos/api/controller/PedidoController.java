package com.agromercado.pedidos.api.controller;

import com.agromercado.pedidos.application.dto.ConfirmarPedidoRequest;
import com.agromercado.pedidos.application.dto.PedidoResponse;
import com.agromercado.pedidos.application.service.PedidoService;
import com.agromercado.pedidos.domain.model.EstadoPedido;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pedidos
 */
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * POST /pedidos/confirmar - Confirma el carrito como pedido
     */
    @PostMapping("/confirmar")
    public ResponseEntity<PedidoResponse> confirmarPedido(
            @RequestHeader("X-User-Id") String clienteId,
            @Valid @RequestBody ConfirmarPedidoRequest request) {

        PedidoResponse pedido = pedidoService.confirmarPedido(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /**
     * GET /pedidos/{id} - Obtiene un pedido específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedido(@PathVariable Long id) {
        PedidoResponse pedido = pedidoService.obtenerPedido(id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * GET /pedidos/mis-pedidos - Lista los pedidos del cliente actual
     */
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarMisPedidos(
            @RequestHeader("X-User-Id") String clienteId) {

        List<PedidoResponse> pedidos = pedidoService.listarPedidosCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /pedidos/zona/{zonaId} - Lista pedidos de una zona (para admin de zona)
     */
    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorZona(@PathVariable String zonaId) {
        List<PedidoResponse> pedidos = pedidoService.listarPedidosPorZona(zonaId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * PATCH /pedidos/{id}/estado - Actualiza el estado del pedido
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido estado) {

        PedidoResponse pedido = pedidoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(pedido);
    }
}
