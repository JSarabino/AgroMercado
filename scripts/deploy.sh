#!/bin/bash

# ============================================
# AgroMercado - Deployment Script
# ============================================
# Este script despliega o actualiza la aplicación
# de manera segura usando Docker Compose
# ============================================

set -e  # Salir si cualquier comando falla

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar que docker-compose esté instalado
if ! command -v docker-compose &> /dev/null; then
    log_error "docker-compose no está instalado"
    exit 1
fi

# Verificar que el archivo .env exista
if [ ! -f .env ]; then
    log_error "Archivo .env no encontrado. Por favor créalo antes de continuar."
    exit 1
fi

log_info "Iniciando proceso de deployment..."

# ============================================
# PASO 1: Backup de bases de datos (opcional)
# ============================================
if [ "$SKIP_BACKUP" != "true" ]; then
    log_info "Creando backup de bases de datos..."
    BACKUP_DIR="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    # Backup PostgreSQL (solo si el contenedor está corriendo)
    if docker ps | grep -q "agromercado-postgres"; then
        log_info "Backing up PostgreSQL..."
        docker-compose exec -T postgres pg_dumpall -U postgres > "$BACKUP_DIR/postgres_backup.sql" 2>/dev/null || log_warning "No se pudo hacer backup de PostgreSQL (puede que no esté corriendo)"
    fi

    # Backup MongoDB (solo si el contenedor está corriendo)
    if docker ps | grep -q "agromercado-mongodb"; then
        log_info "Backing up MongoDB..."
        docker-compose exec -T mongodb mongodump --username root --password root --out /tmp/backup 2>/dev/null || log_warning "No se pudo hacer backup de MongoDB (puede que no esté corriendo)"
        docker cp agromercado-mongodb:/tmp/backup "$BACKUP_DIR/mongodb_backup" 2>/dev/null || true
    fi

    log_success "Backups completados en $BACKUP_DIR"
fi

# ============================================
# PASO 2: Pull de imágenes base
# ============================================
log_info "Pulling imágenes base de Docker Hub..."
docker-compose pull postgres mongodb rabbitmq || log_warning "Algunas imágenes base no pudieron actualizarse"

# ============================================
# PASO 3: Build de imágenes
# ============================================
log_info "Construyendo imágenes de microservicios y frontend..."
log_info "⚠️  Primer deploy: ~15-20 min | Deploys siguientes: ~2-5 min (usando cache)"

# Build usando cache de Docker para mayor velocidad
# Solo reconstruye las capas que cambiaron
docker-compose build 2>&1 | grep -E "(Step|Successfully|ERROR|Pulling|Using cache)" || true

log_success "Imágenes construidas exitosamente"

# ============================================
# PASO 4: Detener servicios antiguos
# ============================================
log_info "Deteniendo servicios antiguos..."
docker-compose down --remove-orphans

# ============================================
# PASO 5: Limpiar recursos no utilizados
# ============================================
log_info "Limpiando recursos Docker no utilizados..."
docker system prune -f --volumes=false || log_warning "No se pudieron limpiar todos los recursos"

# ============================================
# PASO 6: Iniciar servicios en orden
# ============================================
log_info "Iniciando servicios de infraestructura..."

# Primero las bases de datos y RabbitMQ
docker-compose up -d postgres mongodb rabbitmq
log_info "Esperando que los servicios de infraestructura estén listos (45 segundos)..."
sleep 45

# Verificar que las bases de datos estén saludables
log_info "Verificando salud de bases de datos..."
for i in {1..12}; do
    if docker-compose ps postgres | grep -q "healthy"; then
        log_success "PostgreSQL está listo"
        break
    fi
    if [ $i -eq 12 ]; then
        log_error "PostgreSQL no está respondiendo"
        docker-compose logs postgres
        exit 1
    fi
    log_info "Esperando PostgreSQL... ($i/12)"
    sleep 5
done

for i in {1..12}; do
    if docker-compose ps mongodb | grep -q "healthy"; then
        log_success "MongoDB está listo"
        break
    fi
    if [ $i -eq 12 ]; then
        log_error "MongoDB no está respondiendo"
        docker-compose logs mongodb
        exit 1
    fi
    log_info "Esperando MongoDB... ($i/12)"
    sleep 5
done

# Eureka Server
log_info "Iniciando Eureka Server..."
docker-compose up -d eureka-server
log_info "Esperando Eureka Server (60 segundos)..."
sleep 60

# API Gateway y microservicios
log_info "Iniciando API Gateway y microservicios..."
docker-compose up -d api-gateway accounts-service productos-service pedidos-service
log_info "Esperando microservicios (90 segundos)..."
sleep 90

# Frontend
log_info "Iniciando Frontend..."
docker-compose up -d frontend
log_info "Esperando Frontend (30 segundos)..."
sleep 30

# ============================================
# PASO 7: Verificar estado de servicios
# ============================================
log_info "Verificando estado de todos los servicios..."

SERVICES=(
    "postgres"
    "mongodb"
    "rabbitmq"
    "eureka-server"
    "api-gateway"
    "accounts-service"
    "productos-service"
    "pedidos-service"
    "frontend"
)

ALL_RUNNING=true
for service in "${SERVICES[@]}"; do
    if docker-compose ps $service | grep -q "Up"; then
        log_success "$service está corriendo"
    else
        log_error "$service NO está corriendo"
        ALL_RUNNING=false
    fi
done

# ============================================
# PASO 8: Health checks
# ============================================
log_info "Ejecutando health checks..."

# API Gateway
for i in {1..10}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null; then
        log_success "API Gateway health check OK"
        break
    fi
    if [ $i -eq 10 ]; then
        log_warning "API Gateway health check falló (puede estar iniciando)"
    fi
    sleep 6
done

# Frontend
for i in {1..5}; do
    if curl -sf http://localhost:80 > /dev/null; then
        log_success "Frontend health check OK"
        break
    fi
    if [ $i -eq 5 ]; then
        log_warning "Frontend health check falló"
    fi
    sleep 6
done

# ============================================
# PASO 9: Mostrar estado final
# ============================================
echo ""
echo "============================================"
log_success "DEPLOYMENT COMPLETADO"
echo "============================================"
echo ""
log_info "Estado de contenedores:"
docker-compose ps
echo ""
log_info "URLs de acceso:"
echo "  Frontend:        http://localhost"
echo "  API Gateway:     http://localhost:8080"
echo "  Eureka:          http://localhost:8761"
echo "  RabbitMQ Admin:  http://localhost:15672"
echo ""
log_info "Para ver logs en tiempo real:"
echo "  docker-compose logs -f"
echo ""
log_info "Para ver logs de un servicio específico:"
echo "  docker-compose logs -f <service-name>"
echo ""

if [ "$ALL_RUNNING" = true ]; then
    log_success "✅ Todos los servicios están corriendo correctamente"
    exit 0
else
    log_error "❌ Algunos servicios no están corriendo. Revisar logs con: docker-compose logs"
    exit 1
fi
