
package com.agromercado.productos.fachadaServices.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agromercado.productos.domain.entity.Producto;
import com.agromercado.productos.domain.repository.ProductoRepository;
import com.agromercado.productos.fachadaServices.DTO.ProductoDTO;

@Service
public class ProductoServiceImpl implements IProductoService {

	@Autowired
	private ProductoRepository productoRepository;

	@Override
	@Transactional(readOnly = true)
	public List<ProductoDTO> findAll() {
		return productoRepository.findAll().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public ProductoDTO findById(Integer id) {
		Producto producto = productoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
		return convertToDTO(producto);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductoDTO> findByProductorId(String idProductor) {
		return productoRepository.findByIdProductor(idProductor).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductoDTO> findByZonaId(String zonaId) {
		return productoRepository.findByZonaId(zonaId).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductoDTO> findAvailable() {
		return productoRepository.findByDisponibleTrue().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ProductoDTO create(ProductoDTO productoDTO) {
		Producto producto = convertToEntity(productoDTO);
		Producto savedProducto = productoRepository.save(producto);
		return convertToDTO(savedProducto);
	}

	@Override
	@Transactional
	public ProductoDTO update(Integer id, ProductoDTO productoDTO) {
		Producto producto = productoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

		// Actualizar campos
		producto.setNombre(productoDTO.getNombre());
		producto.setCategoria(productoDTO.getCategoria());
		producto.setDescripcion(productoDTO.getDescripcion());
		producto.setStockDisponible(productoDTO.getStockDisponible());
		producto.setUnidadMedida(productoDTO.getUnidadMedida());
		producto.setPrecioUnitario(productoDTO.getPrecioUnitario());
		producto.setImagenUrl(productoDTO.getImagenUrl());
		producto.setDisponible(productoDTO.getDisponible());

		Producto updatedProducto = productoRepository.save(producto);
		return convertToDTO(updatedProducto);
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		if (!productoRepository.existsById(id)) {
			throw new RuntimeException("Producto no encontrado con ID: " + id);
		}
		productoRepository.deleteById(id);
	}

	@Override
	@Transactional
	public ProductoDTO toggleDisponibilidad(Integer id) {
		Producto producto = productoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

		producto.setDisponible(!producto.getDisponible());
		Producto updatedProducto = productoRepository.save(producto);
		return convertToDTO(updatedProducto);
	}

	// Métodos auxiliares de conversión
	private ProductoDTO convertToDTO(Producto producto) {
		ProductoDTO dto = new ProductoDTO();
		dto.setIdProducto(producto.getIdProducto());
		dto.setIdProductor(producto.getIdProductor());
		dto.setZonaId(producto.getZonaId());
		dto.setNombre(producto.getNombre());
		dto.setCategoria(producto.getCategoria());
		dto.setDescripcion(producto.getDescripcion());
		dto.setStockDisponible(producto.getStockDisponible());
		dto.setUnidadMedida(producto.getUnidadMedida());
		dto.setPrecioUnitario(producto.getPrecioUnitario());
		dto.setImagenUrl(producto.getImagenUrl());
		dto.setDisponible(producto.getDisponible());
		dto.setCreatedAt(producto.getCreatedAt());
		dto.setUpdatedAt(producto.getUpdatedAt());
		return dto;
	}

	private Producto convertToEntity(ProductoDTO dto) {
		Producto producto = new Producto();
		producto.setIdProductor(dto.getIdProductor());
		producto.setZonaId(dto.getZonaId());
		producto.setNombre(dto.getNombre());
		producto.setCategoria(dto.getCategoria());
		producto.setDescripcion(dto.getDescripcion());
		producto.setStockDisponible(dto.getStockDisponible());
		producto.setUnidadMedida(dto.getUnidadMedida());
		producto.setPrecioUnitario(dto.getPrecioUnitario());
		producto.setImagenUrl(dto.getImagenUrl());
		producto.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);
		return producto;
	}
}
