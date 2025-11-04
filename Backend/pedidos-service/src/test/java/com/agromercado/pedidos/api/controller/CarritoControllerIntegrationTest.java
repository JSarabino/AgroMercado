package com.agromercado.pedidos.api.controller;

import com.agromercado.pedidos.application.dto.AgregarProductoCarritoRequest;
import com.agromercado.pedidos.domain.model.EstadoPedido;
import com.agromercado.pedidos.domain.model.Pedido;
import com.agromercado.pedidos.domain.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CarritoController - Pruebas de Integración")
class CarritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoRepository pedidoRepository;

    private static final String CLIENT_ID = "cliente-integration-test";
    private static final String CLIENT_NAME = "Test User";
    private static final String CLIENT_EMAIL = "test@test.com";

    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba
        pedidoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /carrito - Debe obtener o crear un carrito para el cliente")
    void debeObtenerOCrearCarrito() throws Exception {
        mockMvc.perform(get("/carrito")
                        .header("X-User-Id", CLIENT_ID)
                        .header("X-User-Name", CLIENT_NAME)
                        .header("X-User-Email", CLIENT_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(CLIENT_ID))
                .andExpect(jsonPath("$.estado").value("CARRITO"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.detalles").isEmpty());
    }

    @Test
    @DisplayName("POST /carrito/agregar - Debe agregar un producto al carrito")
    void debeAgregarProductoAlCarrito() throws Exception {
        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(1L);
        request.setCantidad(3);

        mockMvc.perform(post("/carrito/agregar")
                        .header("X-User-Id", CLIENT_ID)
                        .header("X-User-Name", CLIENT_NAME)
                        .header("X-User-Email", CLIENT_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(CLIENT_ID))
                .andExpect(jsonPath("$.detalles").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /carrito - Debe vaciar el carrito")
    void debeVaciarCarrito() throws Exception {
        // Primero crear un carrito con items
        Pedido carrito = Pedido.builder()
                .clienteId(CLIENT_ID)
                .clienteNombre(CLIENT_NAME)
                .clienteEmail(CLIENT_EMAIL)
                .estado(EstadoPedido.CARRITO)
                .total(BigDecimal.valueOf(10000))
                .build();
        pedidoRepository.save(carrito);

        // Vaciar carrito
        mockMvc.perform(delete("/carrito")
                        .header("X-User-Id", CLIENT_ID))
                .andExpect(status().isNoContent());

        // Verificar que el carrito esté vacío
        mockMvc.perform(get("/carrito")
                        .header("X-User-Id", CLIENT_ID)
                        .header("X-User-Name", CLIENT_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.detalles").isEmpty());
    }

    @Test
    @DisplayName("POST /carrito/agregar - Debe validar cantidad requerida")
    void debeValidarCantidadRequerida() throws Exception {
        AgregarProductoCarritoRequest request = new AgregarProductoCarritoRequest();
        request.setProductoId(1L);
        // No se establece cantidad

        mockMvc.perform(post("/carrito/agregar")
                        .header("X-User-Id", CLIENT_ID)
                        .header("X-User-Name", CLIENT_NAME)
                        .header("X-User-Email", CLIENT_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
