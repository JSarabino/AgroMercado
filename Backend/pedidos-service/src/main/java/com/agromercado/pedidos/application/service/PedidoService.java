package com.agromercado.pedidos.application.service;

import com.agromercado.pedidos.application.dto.*;
import com.agromercado.pedidos.domain.model.*;
import com.agromercado.pedidos.domain.repository.DetallePedidoRepository;
import com.agromercado.pedidos.domain.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoClientService productoClientService;
    private final PagoSimuladoService pagoSimuladoService;

    /**
     * Obtiene o crea el carrito de compras del cliente
     */
    @Transactional
    public PedidoResponse obtenerCarrito(String clienteId, String clienteNombre, String clienteEmail) {
        Pedido carrito = pedidoRepository.findByClienteIdAndEstado(clienteId, EstadoPedido.CARRITO)
                .orElseGet(() -> {
                    log.info("Creando nuevo carrito para cliente {}", clienteId);
                    Pedido nuevoCarrito = Pedido.builder()
                            .clienteId(clienteId)
                            .clienteNombre(clienteNombre)
                            .clienteEmail(clienteEmail)
                            .estado(EstadoPedido.CARRITO)
                            .estadoPago(EstadoPago.PENDIENTE)
                            .total(BigDecimal.ZERO)
                            .build();
                    return pedidoRepository.save(nuevoCarrito);
                });

        return convertirAPedidoResponse(carrito);
    }

    /**
     * Agrega un producto al carrito
     */
    @Transactional
    public PedidoResponse agregarProductoAlCarrito(String clienteId, String clienteNombre,
                                                   String clienteEmail, AgregarProductoCarritoRequest request) {
        // Obtener o crear carrito
        Pedido carrito = pedidoRepository.findByClienteIdAndEstado(clienteId, EstadoPedido.CARRITO)
                .orElseGet(() -> {
                    Pedido nuevoCarrito = Pedido.builder()
                            .clienteId(clienteId)
                            .clienteNombre(clienteNombre)
                            .clienteEmail(clienteEmail)
                            .estado(EstadoPedido.CARRITO)
                            .estadoPago(EstadoPago.PENDIENTE)
                            .total(BigDecimal.ZERO)
                            .build();
                    return pedidoRepository.save(nuevoCarrito);
                });

        // Obtener información del producto
        ProductoClientService.ProductoDTO producto = productoClientService.obtenerProducto(request.getProductoId());

        // Validar disponibilidad (si el campo viene null, asumimos disponibilidad infinita por defecto)
        Integer stockDisponible = producto.getStockDisponible();
        if (stockDisponible != null && stockDisponible < request.getCantidad()) {
            throw new IllegalArgumentException("Cantidad solicitada no disponible. Disponible: " + stockDisponible);
        }

        // Asignar zona del producto al carrito si es el primer producto
        if (carrito.getZonaId() == null && producto.getZonaId() != null) {
            carrito.setZonaId(producto.getZonaId());
            log.info("Zona {} asignada al carrito del cliente {}", producto.getZonaId(), clienteId);
        }

        // Validar que el producto sea de la misma zona que el carrito
        if (carrito.getZonaId() != null && producto.getZonaId() != null
                && !carrito.getZonaId().equals(producto.getZonaId())) {
            throw new IllegalArgumentException("No puedes agregar productos de diferentes zonas al mismo carrito. " +
                    "Este carrito es de la zona: " + carrito.getZonaId());
        }

        // Verificar si el producto ya está en el carrito
        DetallePedido detalleExistente = carrito.getDetalles().stream()
                .filter(d -> d.getProductoId().equals(request.getProductoId()))
                .findFirst()
                .orElse(null);

        if (detalleExistente != null) {
            // Actualizar cantidad
            detalleExistente.setCantidad(detalleExistente.getCantidad() + request.getCantidad());
            detalleExistente.calcularSubtotal();
        } else {
            // Agregar nuevo detalle
            DetallePedido nuevoDetalle = DetallePedido.builder()
                    .pedido(carrito)
                    .productoId(producto.getIdProducto() != null ? producto.getIdProducto().longValue() : null)
                    .productoNombre(producto.getNombre())
                    .productoDescripcion(producto.getDescripcion())
                    .productorId(producto.getIdProductor())
                    .productorNombre(producto.getNombreProductor())
                    .cantidad(request.getCantidad())
                    .precioUnitario(producto.getPrecioUnitario())
                    .unidadMedida(producto.getUnidadMedida())
                    .build();
            nuevoDetalle.calcularSubtotal();
            carrito.agregarDetalle(nuevoDetalle);
        }

        // Recalcular total
        carrito.calcularTotal();
        pedidoRepository.save(carrito);

        log.info("Producto {} agregado al carrito del cliente {}", producto.getNombre(), clienteId);
        return convertirAPedidoResponse(carrito);
    }

    /**
     * Elimina un producto del carrito
     */
    @Transactional
    public PedidoResponse eliminarProductoDelCarrito(String clienteId, Long detalleId) {
        Pedido carrito = pedidoRepository.findByClienteIdAndEstado(clienteId, EstadoPedido.CARRITO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el carrito"));

        DetallePedido detalle = carrito.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el carrito"));

        carrito.eliminarDetalle(detalle);
        detallePedidoRepository.delete(detalle);

        carrito.calcularTotal();
        pedidoRepository.save(carrito);

        log.info("Producto eliminado del carrito del cliente {}", clienteId);
        return convertirAPedidoResponse(carrito);
    }

    /**
     * Vacía el carrito
     */
    @Transactional
    public void vaciarCarrito(String clienteId) {
        Pedido carrito = pedidoRepository.findByClienteIdAndEstado(clienteId, EstadoPedido.CARRITO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el carrito"));

        carrito.getDetalles().clear();
        carrito.setTotal(BigDecimal.ZERO);
        pedidoRepository.save(carrito);

        log.info("Carrito vaciado para cliente {}", clienteId);
    }

    /**
     * Confirma el pedido (lo convierte de carrito a pedido)
     */
    @Transactional
    public PedidoResponse confirmarPedido(String clienteId, ConfirmarPedidoRequest request) {
        Pedido pedido = pedidoRepository.findByClienteIdAndEstado(clienteId, EstadoPedido.CARRITO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el carrito"));

        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        log.info("Confirmando pedido - ZonaId antes de confirmar: {}", pedido.getZonaId());

        // Actualizar información de entrega
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setTelefonoContacto(request.getTelefonoContacto());
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setNotas(request.getNotas());

        // Confirmar pedido
        pedido.confirmar();
        pedidoRepository.save(pedido);

        log.info("Pedido {} confirmado para cliente {} - ZonaId: {}",
            pedido.getNumeroPedido(), clienteId, pedido.getZonaId());
        return convertirAPedidoResponse(pedido);
    }

    /**
     * Procesa el pago del pedido
     */
    @Transactional
    public PagoResponse procesarPago(String clienteId, ProcesarPagoRequest request) {
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!pedido.getClienteId().equals(clienteId)) {
            throw new IllegalArgumentException("No tiene permiso para pagar este pedido");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException("El pedido no está en estado pendiente");
        }

        log.info("Procesando pago para pedido {} - ZonaId: {}", pedido.getNumeroPedido(), pedido.getZonaId());

        // Procesar pago simulado
        PagoResponse pagoResponse = pagoSimuladoService.procesarPago(pedido, request);

        if (pagoResponse.isAprobado()) {
            pedido.marcarComoPagado(pagoResponse.getTransaccionId());
            pedido.setEstado(EstadoPedido.EN_PREPARACION);
            pedidoRepository.save(pedido);
            log.info("Pago aprobado para pedido {} - ZonaId: {} - Estado: {}",
                pedido.getNumeroPedido(), pedido.getZonaId(), pedido.getEstado());
        } else {
            pedido.setEstadoPago(EstadoPago.RECHAZADO);
            pedidoRepository.save(pedido);
            log.warn("Pago rechazado para pedido {} - ZonaId: {}",
                pedido.getNumeroPedido(), pedido.getZonaId());
        }

        return pagoResponse;
    }

    /**
     * Obtiene un pedido por ID
     */
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        return convertirAPedidoResponse(pedido);
    }

    /**
     * Lista los pedidos de un cliente
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosCliente(String clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream()
                .filter(p -> p.getEstado() != EstadoPedido.CARRITO)
                .map(this::convertirAPedidoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pedidos por zona
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorZona(String zonaId) {
        log.info("Buscando pedidos para zona: {}", zonaId);
        List<Pedido> pedidos = pedidoRepository.findPedidosByZona(zonaId);
        log.info("Pedidos encontrados para zona {}: {}", zonaId, pedidos.size());

        // Log adicional para ver el detalle
        pedidos.forEach(p -> log.info("Pedido ID: {}, Estado: {}, ZonaId: {}, ClienteId: {}",
            p.getId(), p.getEstado(), p.getZonaId(), p.getClienteId()));

        return pedidos.stream()
                .map(this::convertirAPedidoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado del pedido
     */
    @Transactional
    public PedidoResponse actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoPedido.ENTREGADO) {
            pedido.setFechaEntrega(java.time.LocalDateTime.now());
        }

        pedidoRepository.save(pedido);
        log.info("Estado del pedido {} actualizado a {}", pedido.getNumeroPedido(), nuevoEstado);

        return convertirAPedidoResponse(pedido);
    }

    // Método auxiliar de conversión
    private PedidoResponse convertirAPedidoResponse(Pedido pedido) {
        List<DetallePedidoDTO> detallesDTO = pedido.getDetalles().stream()
                .map(d -> DetallePedidoDTO.builder()
                        .id(d.getId())
                        .productoId(d.getProductoId())
                        .productoNombre(d.getProductoNombre())
                        .productoDescripcion(d.getProductoDescripcion())
                        .productorId(d.getProductorId())
                        .productorNombre(d.getProductorNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .unidadMedida(d.getUnidadMedida())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .clienteNombre(pedido.getClienteNombre())
                .clienteEmail(pedido.getClienteEmail())
                .zonaId(pedido.getZonaId())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado())
                .estadoPago(pedido.getEstadoPago())
                .metodoPago(pedido.getMetodoPago())
                .subtotal(pedido.getSubtotal())
                .impuestos(pedido.getImpuestos())
                .total(pedido.getTotal())
                .direccionEntrega(pedido.getDireccionEntrega())
                .telefonoContacto(pedido.getTelefonoContacto())
                .notas(pedido.getNotas())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaConfirmacion(pedido.getFechaConfirmacion())
                .fechaPago(pedido.getFechaPago())
                .fechaEntrega(pedido.getFechaEntrega())
                .transaccionPagoId(pedido.getTransaccionPagoId())
                .detalles(detallesDTO)
                .build();
    }
}
