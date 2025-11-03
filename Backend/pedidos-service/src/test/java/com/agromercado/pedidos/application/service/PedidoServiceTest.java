package com.agromercado.pedidos.application.service;

import com.agromercado.pedidos.application.dto.AgregarProductoCarritoRequest;
import com.agromercado.pedidos.application.dto.ConfirmarPedidoRequest;
import com.agromercado.pedidos.application.dto.PedidoResponse;
import com.agromercado.pedidos.domain.model.*;
import com.agromercado.pedidos.domain.repository.DetallePedidoRepository;
import com.agromercado.pedidos.domain.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Pruebas Unitarias")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ProductoClientService productoClientService;

    @Mock
    private PagoSimuladoService pagoSimuladoService;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido carritoMock;
    private ProductoClientService.ProductoDTO productoMock;
    private static final String CLIENT_ID = "cliente-test-123";
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final String CLIENT_EMAIL = "juan@test.com";
    private static final String ZONA_ID = "zona-test-456";

    @BeforeEach
    void setUp() {
        // Mock de carrito
        carritoMock = Pedido.builder()
                .id(1L)
                .clienteId(CLIENT_ID)
                .clienteNombre(CLIENT_NAME)
                .clienteEmail(CLIENT_EMAIL)
                .zonaId(ZONA_ID)
                .estado(EstadoPedido.CARRITO)
                .estadoPago(EstadoPago.PENDIENTE)
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        // Mock de producto
        productoMock = new ProductoClientService.ProductoDTO();
        productoMock.setIdProducto(100);
        productoMock.setNombre("Tomate");
        productoMock.setDescripcion("Tomate fresco");
        productoMock.setPrecioUnitario(new BigDecimal("5000"));
        productoMock.setStockDisponible(100);
        productoMock.setDisponible(true);
        productoMock.setZonaId(ZONA_ID);
        productoMock.setUnidadMedida("kg");
    }

    @Test
    @DisplayName("Debe obtener o crear un carrito nuevo para el cliente")
    void debeObtenerOCrearCarrito() {
        // Arrange
        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.empty());
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(carritoMock);

        // Act
        PedidoResponse resultado = pedidoService.obtenerCarrito(CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL);

        // Assert
        assertNotNull(resultado);
        assertEquals(CLIENT_ID, resultado.getClienteId());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe agregar producto al carrito correctamente")
    void debeAgregarProductoAlCarrito() {
        // Arrange
        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(100L);
        request.setCantidad(5);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(productoClientService.obtenerProducto(100L)).thenReturn(productoMock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(carritoMock);

        // Act
        PedidoResponse resultado = pedidoService.agregarProductoAlCarrito(
                CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, request);

        // Assert
        assertNotNull(resultado);
        verify(productoClientService).obtenerProducto(100L);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay stock disponible")
    void debeLanzarExcepcionSiNoHayStock() {
        // Arrange
        productoMock.setStockDisponible(3);
        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(100L);
        request.setCantidad(10);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(productoClientService.obtenerProducto(100L)).thenReturn(productoMock);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                pedidoService.agregarProductoAlCarrito(CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, request)
        );
    }

    @Test
    @DisplayName("Debe asignar zona al carrito al agregar el primer producto")
    void debeAsignarZonaAlCarrito() {
        // Arrange
        carritoMock.setZonaId(null); // Sin zona asignada
        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(100L);
        request.setCantidad(2);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(productoClientService.obtenerProducto(100L)).thenReturn(productoMock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(carritoMock);

        // Act
        pedidoService.agregarProductoAlCarrito(CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, request);

        // Assert
        assertEquals(ZONA_ID, carritoMock.getZonaId());
    }

    @Test
    @DisplayName("Debe rechazar productos de diferentes zonas en el mismo carrito")
    void debeRechazarProductosDeDiferentesZonas() {
        // Arrange
        carritoMock.setZonaId("zona-diferente");
        productoMock.setZonaId(ZONA_ID);

        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(100L);
        request.setCantidad(2);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(productoClientService.obtenerProducto(100L)).thenReturn(productoMock);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                pedidoService.agregarProductoAlCarrito(CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, request)
        );
    }

    @Test
    @DisplayName("Debe confirmar el pedido correctamente")
    void debeConfirmarPedido() {
        // Arrange
        DetallePedido detalle = DetallePedido.builder()
                .productoId(100L)
                .productoNombre("Tomate")
                .cantidad(5)
                .precioUnitario(new BigDecimal("5000"))
                .build();
        detalle.calcularSubtotal();
        carritoMock.getDetalles().add(detalle);

        ConfirmarPedidoRequest request = new ConfirmarPedidoRequest();
        request.setDireccionEntrega("Calle 123");
        request.setTelefonoContacto("3001234567");
        request.setMetodoPago(MetodoPago.TARJETA_CREDITO);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(carritoMock);

        // Act
        PedidoResponse resultado = pedidoService.confirmarPedido(CLIENT_ID, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Calle 123", resultado.getDireccionEntrega());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al confirmar carrito vacío")
    void debeLanzarExcepcionAlConfirmarCarritoVacio() {
        // Arrange
        ConfirmarPedidoRequest request = new ConfirmarPedidoRequest();
        request.setDireccionEntrega("Calle 123");
        request.setTelefonoContacto("3001234567");
        request.setMetodoPago(MetodoPago.TARJETA_CREDITO);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                pedidoService.confirmarPedido(CLIENT_ID, request)
        );
    }

    @Test
    @DisplayName("Debe vaciar el carrito correctamente")
    void debeVaciarCarrito() {
        // Arrange
        DetallePedido detalle = DetallePedido.builder()
                .id(1L)
                .productoId(100L)
                .cantidad(5)
                .build();
        carritoMock.getDetalles().add(detalle);

        when(pedidoRepository.findByClienteIdAndEstado(CLIENT_ID, EstadoPedido.CARRITO))
                .thenReturn(Optional.of(carritoMock));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(carritoMock);

        // Act
        pedidoService.vaciarCarrito(CLIENT_ID);

        // Assert
        assertTrue(carritoMock.getDetalles().isEmpty());
        assertEquals(BigDecimal.ZERO, carritoMock.getTotal());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe calcular el total del pedido correctamente")
    void debeCalcularTotalCorrectamente() {
        // Arrange
        DetallePedido detalle1 = DetallePedido.builder()
                .productoId(100L)
                .cantidad(5)
                .precioUnitario(new BigDecimal("5000"))
                .build();
        detalle1.calcularSubtotal();

        DetallePedido detalle2 = DetallePedido.builder()
                .productoId(101L)
                .cantidad(3)
                .precioUnitario(new BigDecimal("3000"))
                .build();
        detalle2.calcularSubtotal();

        carritoMock.getDetalles().add(detalle1);
        carritoMock.getDetalles().add(detalle2);

        // Act
        carritoMock.calcularTotal();

        // Assert
        BigDecimal subtotalEsperado = new BigDecimal("34000"); // (5*5000) + (3*3000)
        BigDecimal impuestosEsperados = subtotalEsperado.multiply(new BigDecimal("0.19"));
        BigDecimal totalEsperado = subtotalEsperado.add(impuestosEsperados);

        assertEquals(subtotalEsperado, carritoMock.getSubtotal());
        assertEquals(impuestosEsperados, carritoMock.getImpuestos());
        assertEquals(totalEsperado, carritoMock.getTotal());
    }
}
