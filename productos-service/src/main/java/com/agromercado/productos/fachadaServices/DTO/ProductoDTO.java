package com.agromercado.productos.fachadaServices.DTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
	private Integer idProducto;
	private Integer idProductor;
	private String nombre;
	private String categoria;
	private String descripcion;
	private Integer stockDisponible;
	private String unidadMedida;
	private float precioUnitario;	
	private Date createAt;
	
}