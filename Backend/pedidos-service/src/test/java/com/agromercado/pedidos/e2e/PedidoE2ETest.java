package com.agromercado.pedidos.e2e;

import com.agromercado.pedidos.application.dto.AgregarProductoCarritoRequest;
import com.agromercado.pedidos.application.dto.ConfirmarPedidoRequest;
import com.agromercado.pedidos.application.dto.PedidoResponse;
import com.agromercado.pedidos.domain.model.EstadoPedido;
import com.agromercado.pedidos.domain.model.MetodoPago;
import com.agromercado.pedidos.domain.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Pedidos - Pruebas E2E")
class PedidoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PedidoRepository pedidoRepository;

    private String baseUrl;
    private HttpHeaders headers;

    private static final String CLIENT_ID = "cliente-e2e-test";
    private static final String CLIENT_NAME = "E2E Test User";
    private static final String CLIENT_EMAIL = "e2e@test.com";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", CLIENT_ID);
        headers.set("X-User-Name", CLIENT_NAME);
        headers.set("X-User-Email", CLIENT_EMAIL);

        // Limpiar datos
        pedidoRepository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: Carrito -> Agregar productos -> Confirmar pedido")
    void flujoCompletoCarritoAPedido() {
        // PASO 1: Obtener carrito vacío
        ResponseEntity<PedidoResponse> carritoResponse = restTemplate.exchange(
                baseUrl + "/carrito",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PedidoResponse.class
        );

        assertThat(carritoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(carritoResponse.getBody()).isNotNull();
        assertThat(carritoResponse.getBody().getEstado()).isEqualTo(EstadoPedido.CARRITO);
        assertThat(carritoResponse.getBody().getDetalles()).isEmpty();

        // PASO 2: Agregar producto al carrito
        AgregarProductoCarritoRequest agregarRequest = new AgregarProductoCarritoRequest();
        agregarRequest.setProductoId(1L);
        agregarRequest.setCantidad(5);

        ResponseEntity<PedidoResponse> agregarResponse = restTemplate.exchange(
                baseUrl + "/carrito/agregar",
                HttpMethod.POST,
                new HttpEntity<>(agregarRequest, headers),
                PedidoResponse.class
        );

        assertThat(agregarResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(agregarResponse.getBody()).isNotNull();
        assertThat(agregarResponse.getBody().getDetalles()).isNotEmpty();

        // PASO 3: Confirmar pedido
        ConfirmarPedidoRequest confirmarRequest = new ConfirmarPedidoRequest();
        confirmarRequest.setDireccionEntrega("Calle Test 123");
        confirmarRequest.setTelefonoContacto("3001234567");
        confirmarRequest.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        confirmarRequest.setNotas("Pedido de prueba E2E");

        ResponseEntity<PedidoResponse> confirmarResponse = restTemplate.exchange(
                baseUrl + "/pedidos/confirmar",
                HttpMethod.POST,
                new HttpEntity<>(confirmarRequest, headers),
                PedidoResponse.class
        );

        assertThat(confirmarResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(confirmarResponse.getBody()).isNotNull();
        assertThat(confirmarResponse.getBody().getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(confirmarResponse.getBody().getNumeroPedido()).isNotNull();
        assertThat(confirmarResponse.getBody().getDireccionEntrega()).isEqualTo("Calle Test 123");
    }

    @Test
    @DisplayName("Debe obtener lista de pedidos del cliente")
    void debeObtenerListaDePedidos() {
        ResponseEntity<PedidoResponse[]> response = restTemplate.exchange(
                baseUrl + "/pedidos/mis-pedidos",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PedidoResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Debe vaciar el carrito correctamente")
    void debeVaciarCarrito() {
        // Primero crear un carrito con productos
        AgregarProductoCarritoRequest agregarRequest = new AgregarProductoCarritoRequest();
        agregarRequest.setProductoId(1L);
        agregarRequest.setCantidad(3);

        restTemplate.exchange(
                baseUrl + "/carrito/agregar",
                HttpMethod.POST,
                new HttpEntity<>(agregarRequest, headers),
                PedidoResponse.class
        );

        // Vaciar el carrito
        ResponseEntity<Void> vaciarResponse = restTemplate.exchange(
                baseUrl + "/carrito",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(vaciarResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar que está vacío
        ResponseEntity<PedidoResponse> verificarResponse = restTemplate.exchange(
                baseUrl + "/carrito",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PedidoResponse.class
        );

        assertThat(verificarResponse.getBody()).isNotNull();
        assertThat(verificarResponse.getBody().getDetalles()).isEmpty();
    }
}
