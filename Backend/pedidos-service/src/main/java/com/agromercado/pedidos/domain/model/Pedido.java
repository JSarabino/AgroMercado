package com.agromercado.pedidos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;  // ID del usuario cliente

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "zona_id")
    private String zonaId;  // Zona desde donde se compran los productos

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;  // Número único del pedido

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "impuestos", precision = 10, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "direccion_entrega", length = 500)
    private String direccionEntrega;

    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    @Column(name = "notas", length = 1000)
    private String notas;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "transaccion_pago_id")
    private String transaccionPagoId;  // ID de la transacción de pago simulado

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPedido.CARRITO;
        }
        if (estadoPago == null) {
            estadoPago = EstadoPago.PENDIENTE;
        }
    }

    // Métodos de utilidad
    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void eliminarDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
    }

    public void calcularTotal() {
        // Asegurar que todos los detalles tengan su subtotal calculado
        detalles.forEach(detalle -> {
            if (detalle.getSubtotal() == null) {
                detalle.calcularSubtotal();
            }
        });

        // Sumar subtotales (filtrar nulls por seguridad)
        this.subtotal = detalles.stream()
            .map(DetallePedido::getSubtotal)
            .filter(subtotal -> subtotal != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular impuestos (IVA 19%)
        this.impuestos = subtotal.multiply(new BigDecimal("0.19"));
        this.total = subtotal.add(impuestos);
    }

    public void confirmar() {
        this.estado = EstadoPedido.PENDIENTE;
        this.fechaConfirmacion = LocalDateTime.now();
        this.numeroPedido = generarNumeroPedido();
    }

    public void marcarComoPagado(String transaccionId) {
        this.estado = EstadoPedido.PAGADO;
        this.estadoPago = EstadoPago.APROBADO;
        this.fechaPago = LocalDateTime.now();
        this.transaccionPagoId = transaccionId;
    }

    private String generarNumeroPedido() {
        return "PED-" + System.currentTimeMillis();
    }
}
