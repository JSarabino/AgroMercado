package com.agromercado.pedidos.application.service;

import com.agromercado.pedidos.application.dto.PagoResponse;
import com.agromercado.pedidos.application.dto.ProcesarPagoRequest;
import com.agromercado.pedidos.domain.model.MetodoPago;
import com.agromercado.pedidos.domain.model.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * Servicio de pago simulado para desarrollo y pruebas
 */
@Service
@Slf4j
public class PagoSimuladoService {

    private final Random random = new Random();

    /**
     * Procesa un pago de manera simulada
     * En un sistema real, aquí se integraría con una pasarela de pago como Stripe, PayPal, etc.
     */
    public PagoResponse procesarPago(Pedido pedido, ProcesarPagoRequest request) {
        log.info("Procesando pago simulado para pedido {} por un monto de {}",
                 pedido.getId(), pedido.getTotal());

        // Simular procesamiento de pago (2 segundos)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simular aprobación/rechazo aleatorio (90% de aprobación)
        boolean aprobado = random.nextInt(100) < 90;

        String transaccionId = generarTransaccionId(request.getMetodoPago());
        String mensaje = aprobado ?
            "Pago procesado exitosamente" :
            "Pago rechazado - Fondos insuficientes";

        log.info("Resultado del pago: {} - Transacción: {}",
                 aprobado ? "APROBADO" : "RECHAZADO", transaccionId);

        return PagoResponse.builder()
                .transaccionId(transaccionId)
                .aprobado(aprobado)
                .mensaje(mensaje)
                .monto(pedido.getTotal())
                .metodoPago(request.getMetodoPago().toString())
                .pedidoId(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .build();
    }

    /**
     * Simula un reembolso
     */
    public boolean procesarReembolso(String transaccionId) {
        log.info("Procesando reembolso simulado para transacción {}", transaccionId);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Siempre exitoso en simulación
        return true;
    }

    private String generarTransaccionId(MetodoPago metodoPago) {
        String prefix = switch (metodoPago) {
            case TARJETA_CREDITO -> "TDC";
            case TARJETA_DEBITO -> "TDD";
            case TRANSFERENCIA -> "TRF";
            case PSE -> "PSE";
            case EFECTIVO -> "EFE";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
