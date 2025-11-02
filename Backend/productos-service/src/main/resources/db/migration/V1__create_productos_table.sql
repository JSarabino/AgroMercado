-- Crear tabla de productos
CREATE TABLE IF NOT EXISTS productos (
  id_producto           SERIAL PRIMARY KEY,
  id_productor          VARCHAR(120) NOT NULL,  -- ID del usuario productor
  zona_id               VARCHAR(50) NOT NULL,    -- Zona donde se publica el producto
  nombre                VARCHAR(200) NOT NULL,
  categoria             VARCHAR(100) NOT NULL,
  descripcion           TEXT,
  stock_disponible      INTEGER NOT NULL DEFAULT 0,
  unidad_medida         VARCHAR(50) NOT NULL,    -- kg, unidad, litro, etc.
  precio_unitario       DECIMAL(10,2) NOT NULL,
  imagen_url            VARCHAR(500),
  disponible            BOOLEAN NOT NULL DEFAULT true,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS ix_productos_productor ON productos(id_productor);
CREATE INDEX IF NOT EXISTS ix_productos_zona ON productos(zona_id);
CREATE INDEX IF NOT EXISTS ix_productos_categoria ON productos(categoria);
CREATE INDEX IF NOT EXISTS ix_productos_disponible ON productos(disponible);

-- Comentarios para documentación
COMMENT ON TABLE productos IS 'Productos publicados por productores en sus zonas';
COMMENT ON COLUMN productos.id_productor IS 'ID del usuario productor que publica el producto';
COMMENT ON COLUMN productos.zona_id IS 'Zona productiva donde se publica el producto';
COMMENT ON COLUMN productos.disponible IS 'Indica si el producto está disponible para compra';
