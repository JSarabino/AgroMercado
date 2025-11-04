-- Script para recrear la base de datos accounts_cmd
-- Ejecutar este script en PostgreSQL con el usuario postgres

-- Terminar todas las conexiones activas a la base de datos
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'accounts_cmd'
  AND pid <> pg_backend_pid();

-- Eliminar la base de datos si existe
DROP DATABASE IF EXISTS accounts_cmd;

-- Crear la base de datos nuevamente
CREATE DATABASE accounts_cmd
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Colombia.1252'
    LC_CTYPE = 'Spanish_Colombia.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Conectarse a la base de datos accounts_cmd y continuar
\c accounts_cmd

-- Flyway aplicará automáticamente todas las migraciones cuando se inicie el servicio
