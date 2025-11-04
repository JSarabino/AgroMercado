package com.agromercado.pedidos.application.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Cliente para comunicarse con el microservicio de productos
 */
@Service
@Slf4j
public class ProductoClientService {

    private final RestTemplate restTemplate;

    @Value("${productos.service.url:http://localhost:5001}")
    private String productosServiceUrl;

    public ProductoClientService() {
        this.restTemplate = new RestTemplate();
    }

    public ProductoDTO obtenerProducto(Long productoId) {
        try {
            String url = productosServiceUrl + "/productos/" + productoId;
            log.info("Obteniendo producto {} desde {}", productoId, url);
            return restTemplate.getForObject(url, ProductoDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener producto {}: {}", productoId, e.getMessage());
            throw new RuntimeException("No se pudo obtener la información del producto: " + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoDTO {
        private Integer idProducto;  // Coincide con el DTO del productos-service
        private String nombre;
        private String descripcion;
        private BigDecimal precioUnitario;  // Coincide con el DTO del productos-service
        private String unidadMedida;
        private Integer stockDisponible;  // Coincide con el DTO del productos-service
        private Boolean disponible;  // Coincide con el DTO del productos-service
        private String idProductor;
        private String zonaId;
        // Campos adicionales que no vienen del servicio pero pueden ser útiles
        private String nombreProductor;
    }
}
