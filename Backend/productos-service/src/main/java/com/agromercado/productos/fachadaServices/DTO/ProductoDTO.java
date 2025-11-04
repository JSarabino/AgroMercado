package com.agromercado.productos.fachadaServices.DTO;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
	private Integer idProducto;

	private String idProductor;  // ID del usuario productor

	@NotBlank(message = "La zona es obligatoria")
	private String zonaId;       // Zona donde se publica el producto

	@NotBlank(message = "El nombre del producto es obligatorio")
	@Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
	private String nombre;

	@NotBlank(message = "La categoría es obligatoria")
	@Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
	private String categoria;

	private String descripcion;

	@NotNull(message = "El stock es obligatorio")
	@Min(value = 0, message = "El stock no puede ser negativo")
	private Integer stockDisponible;

	@NotBlank(message = "La unidad de medida es obligatoria")
	@Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
	private String unidadMedida;

	@NotNull(message = "El precio es obligatorio")
	@DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
	private BigDecimal precioUnitario;

	private String imagenUrl;

	private Boolean disponible = true;

	private Instant createdAt;

	private Instant updatedAt;
}