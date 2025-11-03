-- Script para inicializar la base de datos de pedidos
-- Ejecutar como usuario postgres

-- Crear base de datos si no existe
SELECT 'CREATE DATABASE pedidos_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pedidos_db')\gexec

-- Conectarse a la base de datos
\c pedidos_db

-- Dar permisos al usuario postgres
GRANT ALL PRIVILEGES ON DATABASE pedidos_db TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;

-- Mensaje de confirmación
SELECT 'Base de datos pedidos_db inicializada correctamente' AS status;
