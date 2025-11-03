package com.agromercado.pedidos.api.controller;

import com.agromercado.pedidos.application.dto.AgregarProductoCarritoRequest;
import com.agromercado.pedidos.application.dto.PedidoResponse;
import com.agromercado.pedidos.application.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión del carrito de compras
 */
@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final PedidoService pedidoService;

    /**
     * GET /carrito - Obtiene el carrito del cliente actual
     */
    @GetMapping
    public ResponseEntity<PedidoResponse> obtenerCarrito(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "dev-user") String clienteId,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "Usuario Demo") String clienteNombre,
            @RequestHeader(value = "X-User-Email", required = false) String clienteEmail) {

        PedidoResponse carrito = pedidoService.obtenerCarrito(clienteId, clienteNombre, clienteEmail);
        return ResponseEntity.ok(carrito);
    }

    /**
     * POST /carrito/agregar - Agrega un producto al carrito
     */
    @PostMapping("/agregar")
    public ResponseEntity<PedidoResponse> agregarProducto(
            @RequestHeader("X-User-Id") String clienteId,
            @RequestHeader("X-User-Name") String clienteNombre,
            @RequestHeader(value = "X-User-Email", required = false) String clienteEmail,
            @Valid @RequestBody AgregarProductoCarritoRequest request) {

        PedidoResponse carrito = pedidoService.agregarProductoAlCarrito(
                clienteId, clienteNombre, clienteEmail, request);
        return ResponseEntity.ok(carrito);
    }

    /**
     * DELETE /carrito/items/{detalleId} - Elimina un producto del carrito
     */
    @DeleteMapping("/items/{detalleId}")
    public ResponseEntity<PedidoResponse> eliminarProducto(
            @RequestHeader("X-User-Id") String clienteId,
            @PathVariable Long detalleId) {

        PedidoResponse carrito = pedidoService.eliminarProductoDelCarrito(clienteId, detalleId);
        return ResponseEntity.ok(carrito);
    }

    /**
     * DELETE /carrito - Vacía el carrito
     */
    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(@RequestHeader("X-User-Id") String clienteId) {
        pedidoService.vaciarCarrito(clienteId);
        return ResponseEntity.noContent().build();
    }
}
