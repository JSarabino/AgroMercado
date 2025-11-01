-- Añadir columna password_hash a la tabla usuarios
ALTER TABLE usuarios
ADD COLUMN password_hash VARCHAR(255);

-- Establecer contraseña para el admin global: admin123
UPDATE usuarios
SET password_hash = '$2b$10$y5/dM4mC3uIe.jKIlWorOutakRqkUVtirOsZNBtQ7fvjaz3Nj8/Zq'
WHERE usuario_id = 'USR-ADMIN-GLOBAL';

-- Para otros usuarios existentes sin contraseña, establecer un valor temporal
UPDATE usuarios
SET password_hash = '$2a$10$dummyhash'
WHERE password_hash IS NULL;

-- Hacer el campo obligatorio después de actualizar datos existentes
-- ALTER TABLE usuarios ALTER COLUMN password_hash SET NOT NULL;
-- Nota: Comentado para permitir migración gradual. Descomentar en producción después de migrar usuarios.
