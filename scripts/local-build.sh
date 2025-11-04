#!/bin/bash

# Script para construir y probar localmente antes de desplegar

set -e

echo "================================================"
echo "AgroMercado - Build Local"
echo "================================================"

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: Este script debe ejecutarse desde la raíz del proyecto"
    exit 1
fi

# Verificar que existe .env
if [ ! -f ".env" ]; then
    echo "Advertencia: No se encontró .env, usando valores por defecto"
    cp env.example .env
fi

echo ""
echo "1. Deteniendo contenedores existentes..."
docker-compose down

echo ""
echo "2. Limpiando imágenes anteriores..."
read -p "¿Desea limpiar las imágenes anteriores? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose down --rmi local
fi

echo ""
echo "3. Construyendo imágenes..."
docker-compose build

echo ""
echo "4. Iniciando servicios..."
docker-compose up -d

echo ""
echo "5. Esperando a que los servicios estén listos..."
sleep 30

echo ""
echo "6. Verificando estado de los servicios..."
docker-compose ps

echo ""
echo "================================================"
echo "Build completado!"
echo "================================================"
echo ""
echo "URLs disponibles:"
echo "  - Frontend:      http://localhost"
echo "  - API Gateway:   http://localhost:8080"
echo "  - Eureka:        http://localhost:8761"
echo "  - RabbitMQ Mgmt: http://localhost:15672"
echo ""
echo "Para ver logs: docker-compose logs -f"
echo "Para detener:  docker-compose down"
echo ""
