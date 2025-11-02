package com.agromercado.productos.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agromercado.productos.domain.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Buscar productos por productor
    List<Producto> findByIdProductor(String idProductor);

    // Buscar productos por zona
    List<Producto> findByZonaId(String zonaId);

    // Buscar productos disponibles
    List<Producto> findByDisponibleTrue();

    // Buscar productos por categoria
    List<Producto> findByCategoria(String categoria);

    // Buscar productos por productor y zona
    List<Producto> findByIdProductorAndZonaId(String idProductor, String zonaId);

    // Buscar productos disponibles por zona
    List<Producto> findByZonaIdAndDisponibleTrue(String zonaId);
}
