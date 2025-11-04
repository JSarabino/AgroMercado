#!/bin/bash

# Script para verificar la salud de todos los servicios

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para verificar un endpoint
check_endpoint() {
    local name=$1
    local url=$2
    local expected_code=${3:-200}

    echo -n "Verificando $name... "

    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)

    if [ "$response" = "$expected_code" ]; then
        echo -e "${GREEN}✓ OK${NC} (HTTP $response)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $response)"
        return 1
    fi
}

# Función para verificar un contenedor Docker
check_container() {
    local name=$1

    echo -n "Verificando contenedor $name... "

    if docker ps --format '{{.Names}}' | grep -q "$name"; then
        status=$(docker inspect --format='{{.State.Health.Status}}' "$name" 2>/dev/null || echo "unknown")
        if [ "$status" = "healthy" ] || [ "$status" = "unknown" ]; then
            echo -e "${GREEN}✓ Running${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠ Running but unhealthy${NC}"
            return 1
        fi
    else
        echo -e "${RED}✗ Not running${NC}"
        return 1
    fi
}

echo "================================================"
echo "AgroMercado - Health Check"
echo "================================================"
echo ""

# Obtener el host (localhost o IP de EC2)
HOST=${1:-localhost}

echo "Verificando en: $HOST"
echo ""

# Verificar contenedores Docker
echo "--- Infraestructura ---"
check_container "agromercado-postgres"
check_container "agromercado-mongodb"
check_container "agromercado-rabbitmq"

echo ""
echo "--- Microservicios ---"
check_container "agromercado-eureka"
check_container "agromercado-gateway"
check_container "agromercado-accounts"
check_container "agromercado-productos"
check_container "agromercado-pedidos"

echo ""
echo "--- Frontend ---"
check_container "agromercado-frontend"

echo ""
echo "--- Endpoints HTTP ---"

# Verificar endpoints
check_endpoint "Eureka Server" "http://$HOST:8761/actuator/health"
check_endpoint "API Gateway" "http://$HOST:8080/actuator/health"
check_endpoint "Accounts Service" "http://$HOST:8081/actuator/health" || true
check_endpoint "Productos Service" "http://$HOST:5001/actuator/health" || true
check_endpoint "Pedidos Service" "http://$HOST:5003/actuator/health" || true
check_endpoint "Frontend" "http://$HOST/health"

echo ""
echo "================================================"
echo "Verificación completada"
echo "================================================"
echo ""
echo "Para ver logs: docker-compose logs -f"
echo "Para ver servicios registrados: http://$HOST:8761"
echo ""
