#!/bin/bash

# Script para hacer backup de las bases de datos

set -e

BACKUP_DIR="${1:-./backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "================================================"
echo "AgroMercado - Backup Database"
echo "================================================"
echo ""

# Crear directorio de backups si no existe
mkdir -p "$BACKUP_DIR"

echo "Directorio de backup: $BACKUP_DIR"
echo ""

# Backup PostgreSQL
echo "1. Haciendo backup de PostgreSQL..."
docker-compose exec -T postgres pg_dumpall -U postgres > "$BACKUP_DIR/postgres_backup_$TIMESTAMP.sql"
echo "   ✓ PostgreSQL backup guardado: postgres_backup_$TIMESTAMP.sql"

# Backup MongoDB
echo ""
echo "2. Haciendo backup de MongoDB..."
docker-compose exec -T mongodb mongodump --username root --password root --authenticationDatabase admin --archive > "$BACKUP_DIR/mongodb_backup_$TIMESTAMP.archive"
echo "   ✓ MongoDB backup guardado: mongodb_backup_$TIMESTAMP.archive"

echo ""
echo "================================================"
echo "Backup completado!"
echo "================================================"
echo ""
echo "Archivos creados:"
ls -lh "$BACKUP_DIR"/*_$TIMESTAMP.*

echo ""
echo "Para restaurar PostgreSQL:"
echo "  cat $BACKUP_DIR/postgres_backup_$TIMESTAMP.sql | docker-compose exec -T postgres psql -U postgres"
echo ""
echo "Para restaurar MongoDB:"
echo "  cat $BACKUP_DIR/mongodb_backup_$TIMESTAMP.archive | docker-compose exec -T mongodb mongorestore --username root --password root --authenticationDatabase admin --archive"
echo ""
