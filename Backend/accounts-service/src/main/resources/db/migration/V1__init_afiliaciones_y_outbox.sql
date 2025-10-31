-- === Tabla principal del agregado AfiliacionZona ===
CREATE TABLE IF NOT EXISTS afiliaciones_zona (
  afiliacion_id           VARCHAR(50)  PRIMARY KEY,
  zona_id                 VARCHAR(50)  NOT NULL,
  solicitante_usuario_id  VARCHAR(120) NOT NULL,

  nombre_vereda           VARCHAR(120) NOT NULL,
  municipio               VARCHAR(120) NOT NULL,

  telefono                VARCHAR(40),
  correo                  VARCHAR(255),

  representante_nombre    VARCHAR(120) NOT NULL,
  representante_documento VARCHAR(60)  NOT NULL,
  representante_correo    VARCHAR(255) NOT NULL,

  estado                  VARCHAR(40)  NOT NULL,
  version                 INT          NOT NULL,
  created_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_afiliaciones_afiliacion_id ON afiliaciones_zona(afiliacion_id);
CREATE INDEX IF NOT EXISTS ix_afiliaciones_zona_id ON afiliaciones_zona(zona_id);

-- === Tabla Outbox para eventos de dominio ===
CREATE TABLE IF NOT EXISTS outbox (
  id           BIGSERIAL PRIMARY KEY,
  event_id     VARCHAR(50)  NOT NULL UNIQUE,
  event_type   VARCHAR(120) NOT NULL,
  payload      TEXT         NOT NULL,
  aggregate_id VARCHAR(50),
  zona_id      VARCHAR(50),
  status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  occurred_at  TIMESTAMPTZ  NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  sent_at      TIMESTAMPTZ,
  last_error   VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS ix_outbox_status_occ ON outbox(status, occurred_at);
