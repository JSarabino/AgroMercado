
package com.agromercado.productos.fachadaServices.services;

import java.util.List;

import com.agromercado.productos.fachadaServices.DTO.ProductoDTO;


public interface IProductoService {

	public List<ProductoDTO> findAll();

	public List<ProductoDTO> findByProductorId(Integer idProductor);

	public ProductoDTO findById(Integer id);
}
