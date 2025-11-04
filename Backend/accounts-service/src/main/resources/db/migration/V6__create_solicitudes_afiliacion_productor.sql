-- === Tabla para solicitudes de afiliación de productores a zonas ===
-- Esta tabla es independiente de afiliaciones_zona (que son solicitudes de zonas al admin global)
-- Aquí se almacenan las solicitudes de PRODUCTORES que quieren afiliarse a una ZONA existente

CREATE TABLE IF NOT EXISTS solicitudes_afiliacion_productor (
  solicitud_id            VARCHAR(50)  PRIMARY KEY,
  zona_id                 VARCHAR(50)  NOT NULL,
  productor_usuario_id    VARCHAR(120) NOT NULL,

  -- Datos del productor
  nombre_productor        VARCHAR(120) NOT NULL,
  documento               VARCHAR(60)  NOT NULL,
  telefono                VARCHAR(40),
  correo                  VARCHAR(255),
  direccion               VARCHAR(255),
  tipo_productos          VARCHAR(500), -- puede ser JSON o lista separada por comas

  -- Estado de la solicitud
  estado                  VARCHAR(40)  NOT NULL, -- PENDIENTE, APROBADA, RECHAZADA
  observaciones           TEXT,

  -- Auditoría
  aprobada_por            VARCHAR(120), -- ID del admin de zona que aprobó/rechazó
  fecha_decision          TIMESTAMPTZ,

  version                 INT          NOT NULL DEFAULT 1,
  created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índices para mejorar el rendimiento
CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitudes_productor_id ON solicitudes_afiliacion_productor(solicitud_id);
CREATE INDEX IF NOT EXISTS ix_solicitudes_zona_id ON solicitudes_afiliacion_productor(zona_id);
CREATE INDEX IF NOT EXISTS ix_solicitudes_productor_id ON solicitudes_afiliacion_productor(productor_usuario_id);
CREATE INDEX IF NOT EXISTS ix_solicitudes_estado ON solicitudes_afiliacion_productor(estado);

-- Índice compuesto para buscar solicitudes pendientes de una zona específica
CREATE INDEX IF NOT EXISTS ix_solicitudes_zona_estado ON solicitudes_afiliacion_productor(zona_id, estado);

-- Evitar solicitudes duplicadas del mismo productor a la misma zona (solo si está pendiente)
CREATE UNIQUE INDEX IF NOT EXISTS ux_solicitudes_productor_zona_pendiente
  ON solicitudes_afiliacion_productor(productor_usuario_id, zona_id)
  WHERE estado = 'PENDIENTE';
