package com.agromercado.pedidos.domain.repository;

import com.agromercado.pedidos.domain.model.EstadoPedido;
import com.agromercado.pedidos.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByClienteIdAndEstado(String clienteId, EstadoPedido estado);

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(String clienteId);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    @Query("SELECT p FROM Pedido p WHERE p.zonaId = :zonaId AND p.estado != 'CARRITO' ORDER BY p.fechaCreacion DESC")
    List<Pedido> findPedidosByZona(@Param("zonaId") String zonaId);

    @Query("SELECT p FROM Pedido p JOIN p.detalles d WHERE d.productorId = :productorId AND p.estado != 'CARRITO' ORDER BY p.fechaCreacion DESC")
    List<Pedido> findPedidosByProductor(@Param("productorId") String productorId);
}
