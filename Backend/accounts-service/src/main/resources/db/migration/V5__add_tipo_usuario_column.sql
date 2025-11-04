-- Agregar columna tipo_usuario a la tabla usuarios
ALTER TABLE usuarios
ADD COLUMN tipo_usuario VARCHAR(30) NOT NULL DEFAULT 'CLIENTE';

-- Actualizar el admin global existente
UPDATE usuarios
SET tipo_usuario = 'ADMIN_GLOBAL'
WHERE usuario_id = 'USR-ADMIN-GLOBAL';

-- Crear índice para búsquedas por tipo de usuario
CREATE INDEX IF NOT EXISTS ix_usuarios_tipo_usuario
  ON usuarios(tipo_usuario);

-- Comentarios para documentación
COMMENT ON COLUMN usuarios.tipo_usuario IS 'Tipo de usuario: CLIENTE, PRODUCTOR, ADMIN_ZONA, ADMIN_GLOBAL';
