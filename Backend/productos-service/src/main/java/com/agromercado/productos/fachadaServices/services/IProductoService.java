
package com.agromercado.productos.fachadaServices.services;

import java.util.List;

import com.agromercado.productos.fachadaServices.DTO.ProductoDTO;

public interface IProductoService {

	// Listar todos los productos
	List<ProductoDTO> findAll();

	// Buscar producto por ID
	ProductoDTO findById(Integer id);

	// Buscar productos por productor
	List<ProductoDTO> findByProductorId(String idProductor);

	// Buscar productos por zona
	List<ProductoDTO> findByZonaId(String zonaId);

	// Buscar productos disponibles
	List<ProductoDTO> findAvailable();

	// Crear producto
	ProductoDTO create(ProductoDTO productoDTO);

	// Actualizar producto
	ProductoDTO update(Integer id, ProductoDTO productoDTO);

	// Eliminar producto
	void delete(Integer id);

	// Cambiar disponibilidad
	ProductoDTO toggleDisponibilidad(Integer id);
}
