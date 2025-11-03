-- Tabla principal de pedidos
CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id VARCHAR(100) NOT NULL,
    cliente_nombre VARCHAR(255),
    cliente_email VARCHAR(255),
    zona_id VARCHAR(100),
    numero_pedido VARCHAR(50) UNIQUE,
    estado VARCHAR(20) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(20),
    subtotal DECIMAL(10, 2),
    impuestos DECIMAL(10, 2),
    total DECIMAL(10, 2) NOT NULL,
    direccion_entrega VARCHAR(500),
    telefono_contacto VARCHAR(20),
    notas VARCHAR(1000),
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_confirmacion TIMESTAMP,
    fecha_pago TIMESTAMP,
    fecha_entrega TIMESTAMP,
    transaccion_pago_id VARCHAR(100)
);

-- Tabla de detalles de pedido
CREATE TABLE detalles_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    producto_nombre VARCHAR(255) NOT NULL,
    producto_descripcion TEXT,
    productor_id VARCHAR(100),
    productor_nombre VARCHAR(255),
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    unidad_medida VARCHAR(50),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_numero_pedido ON pedidos(numero_pedido);
CREATE INDEX idx_pedidos_zona_id ON pedidos(zona_id);
CREATE INDEX idx_pedidos_fecha_creacion ON pedidos(fecha_creacion DESC);
CREATE INDEX idx_detalles_pedido_pedido_id ON detalles_pedido(pedido_id);
CREATE INDEX idx_detalles_pedido_producto_id ON detalles_pedido(producto_id);
CREATE INDEX idx_detalles_pedido_productor_id ON detalles_pedido(productor_id);

-- Comentarios para documentación
COMMENT ON TABLE pedidos IS 'Tabla principal que almacena los pedidos y carritos de compra';
COMMENT ON TABLE detalles_pedido IS 'Detalles de los productos en cada pedido';
COMMENT ON COLUMN pedidos.estado IS 'Estados: CARRITO, PENDIENTE, PAGADO, EN_PREPARACION, ENVIADO, ENTREGADO, CANCELADO';
COMMENT ON COLUMN pedidos.estado_pago IS 'Estados de pago: PENDIENTE, PROCESANDO, APROBADO, RECHAZADO, REEMBOLSADO';
