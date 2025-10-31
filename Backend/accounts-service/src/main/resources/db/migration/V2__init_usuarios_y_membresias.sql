-- === Usuarios (CMD / Postgres) ===
CREATE TABLE IF NOT EXISTS usuarios (
  usuario_id     VARCHAR(50)  PRIMARY KEY,        -- USR-<uuid>
  email          VARCHAR(255) NOT NULL UNIQUE,
  nombre         VARCHAR(120) NOT NULL,
  estado_usuario VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO', -- ACTIVO | BLOQUEADO | ELIMINADO
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- === Roles globales (e.g., ADMIN_GLOBAL) ===
CREATE TABLE IF NOT EXISTS usuarios_roles_globales (
  usuario_id  VARCHAR(50)  NOT NULL,
  rol_global  VARCHAR(60)  NOT NULL,
  granted_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  PRIMARY KEY (usuario_id, rol_global),
  CONSTRAINT fk_urg_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_usuarios_roles_globales_usuario
  ON usuarios_roles_globales(usuario_id);

-- === Membresías zonales (ADMIN_ZONA | PRODUCTOR) ===
CREATE TABLE IF NOT EXISTS usuarios_membresias (
  usuario_id  VARCHAR(50)  NOT NULL,
  zona_id     VARCHAR(50)  NOT NULL,
  rol_zonal   VARCHAR(60)  NOT NULL,
  estado      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVA', -- ACTIVA | SUSPENDIDA | REVOCADA
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  PRIMARY KEY (usuario_id, zona_id, rol_zonal, estado),
  CONSTRAINT fk_um_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_usuarios_membresias_usuario
  ON usuarios_membresias(usuario_id);
