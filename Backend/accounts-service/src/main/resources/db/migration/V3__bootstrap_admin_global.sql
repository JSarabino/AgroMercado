-- Bootstrap de un ADMIN_GLOBAL (se completará en migraciones posteriores)
INSERT INTO usuarios (usuario_id, email, nombre, estado_usuario, created_at)
VALUES ('USR-ADMIN-GLOBAL', 'root@agromercado.local', 'Admin Global Bootstrap', 'ACTIVO', now())
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO usuarios_roles_globales (usuario_id, rol_global, granted_at)
VALUES ('USR-ADMIN-GLOBAL', 'ADMIN_GLOBAL', now())
ON CONFLICT (usuario_id, rol_global) DO NOTHING;
