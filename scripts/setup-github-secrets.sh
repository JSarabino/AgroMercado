#!/bin/bash

# ============================================
# Script Helper para Configurar GitHub Secrets
# ============================================
# Este script te ayuda a preparar los valores
# que necesitas agregar como secrets en GitHub
# ============================================

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}   GitHub Secrets Configuration Helper${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}Error: Este script debe ejecutarse desde la raíz del proyecto${NC}"
    exit 1
fi

# Función para leer input con valor por defecto
read_with_default() {
    local prompt="$1"
    local default="$2"
    local value

    if [ -n "$default" ]; then
        read -p "$prompt [$default]: " value
        echo "${value:-$default}"
    else
        read -p "$prompt: " value
        echo "$value"
    fi
}

# ============================================
# 1. Información del EC2
# ============================================
echo -e "${CYAN}1. Información de tu instancia EC2${NC}"
echo ""

# Intentar obtener IP desde Pulumi
if [ -d "infrastructure/pulumi" ]; then
    echo -e "${YELLOW}Intentando obtener IP desde Pulumi...${NC}"
    cd infrastructure/pulumi
    PULUMI_IP=$(pulumi stack output publicIp 2>/dev/null || echo "")
    cd ../..
    if [ -n "$PULUMI_IP" ]; then
        echo -e "${GREEN}✓ IP encontrada desde Pulumi: $PULUMI_IP${NC}"
    fi
fi

EC2_HOST=$(read_with_default "IP pública de tu EC2" "${PULUMI_IP}")
EC2_USER=$(read_with_default "Usuario SSH" "ubuntu")
DEPLOY_PATH=$(read_with_default "Ruta del código en EC2" "/home/ubuntu/agromercado")

echo ""
echo -e "${CYAN}2. Ruta de tu clave SSH privada${NC}"
echo ""
echo "Por favor ingresa la ruta completa a tu archivo .pem"
echo "Ejemplo: ~/.ssh/mi-clave-ec2.pem"
echo ""

SSH_KEY_PATH=$(read_with_default "Ruta de la clave SSH" "~/.ssh/agromercado.pem")
SSH_KEY_PATH="${SSH_KEY_PATH/#\~/$HOME}"

if [ ! -f "$SSH_KEY_PATH" ]; then
    echo -e "${RED}⚠️  Advertencia: No se encuentra el archivo $SSH_KEY_PATH${NC}"
    echo "Asegúrate de que la ruta sea correcta"
fi

echo ""
echo -e "${CYAN}3. Contraseñas y Secrets${NC}"
echo ""

# Generar passwords seguros
RANDOM_PASS=$(openssl rand -base64 16 2>/dev/null || echo "ChangeMe123!")
RANDOM_JWT=$(openssl rand -base64 32 2>/dev/null || echo "ChangeMe-JWT-Secret-Key-Here")

POSTGRES_PASSWORD=$(read_with_default "Password de PostgreSQL" "$RANDOM_PASS")
MONGO_ROOT_PASSWORD=$(read_with_default "Password de MongoDB" "$RANDOM_PASS")
RABBITMQ_PASS=$(read_with_default "Password de RabbitMQ" "$RANDOM_PASS")
JWT_SECRET=$(read_with_default "JWT Secret" "$RANDOM_JWT")

echo ""
echo -e "${CYAN}4. URLs del Frontend${NC}"
echo ""
VITE_API_BASE_URL=$(read_with_default "VITE_API_BASE_URL" "http://${EC2_HOST}:8080")
VITE_GATEWAY_URL=$(read_with_default "VITE_GATEWAY_URL" "http://${EC2_HOST}:8080")

# ============================================
# Generar resumen
# ============================================
echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}   Resumen de Secrets a Configurar${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""

# Crear archivo temporal con los secrets
SECRETS_FILE="github-secrets-$(date +%Y%m%d-%H%M%S).txt"

cat > "$SECRETS_FILE" << EOF
============================================
GitHub Secrets Configuration
Generado: $(date)
============================================

⚠️  IMPORTANTE: Este archivo contiene información sensible.
    - NO lo subas a Git
    - Elimínalo después de configurar los secrets
    - Guárdalo en un lugar seguro (ej: password manager)

============================================
CONFIGURACIÓN EN GITHUB
============================================

Ve a: Settings → Secrets and variables → Actions → New repository secret

Crea los siguientes secrets (copia y pega el valor de cada uno):

--------------------------------------------
1. EC2_SSH_PRIVATE_KEY
--------------------------------------------
Valor:
EOF

if [ -f "$SSH_KEY_PATH" ]; then
    cat "$SSH_KEY_PATH" >> "$SECRETS_FILE"
else
    echo "[COPIAR CONTENIDO DE $SSH_KEY_PATH]" >> "$SECRETS_FILE"
fi

cat >> "$SECRETS_FILE" << EOF

--------------------------------------------
2. EC2_HOST
--------------------------------------------
$EC2_HOST

--------------------------------------------
3. EC2_USER
--------------------------------------------
$EC2_USER

--------------------------------------------
4. DEPLOY_PATH
--------------------------------------------
$DEPLOY_PATH

--------------------------------------------
5. POSTGRES_PASSWORD
--------------------------------------------
$POSTGRES_PASSWORD

--------------------------------------------
6. MONGO_ROOT_PASSWORD
--------------------------------------------
$MONGO_ROOT_PASSWORD

--------------------------------------------
7. RABBITMQ_PASS
--------------------------------------------
$RABBITMQ_PASS

--------------------------------------------
8. JWT_SECRET
--------------------------------------------
$JWT_SECRET

--------------------------------------------
9. VITE_API_BASE_URL
--------------------------------------------
$VITE_API_BASE_URL

--------------------------------------------
10. VITE_GATEWAY_URL
--------------------------------------------
$VITE_GATEWAY_URL

============================================
ARCHIVO .ENV PARA EL EC2
============================================

Copia este contenido al archivo .env en el EC2:

SPRING_PROFILES_ACTIVE=prod

# Database - PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
POSTGRES_PORT=5432

# Database - MongoDB
MONGO_ROOT_USER=root
MONGO_ROOT_PASSWORD=$MONGO_ROOT_PASSWORD
MONGO_PORT=27017

# Message Broker - RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=$RABBITMQ_PASS
RABBITMQ_PORT=5672
RABBITMQ_MGMT_PORT=15672

# Microservices Ports
API_GATEWAY_PORT=8080
FRONTEND_PORT=80

# Security
JWT_SECRET=$JWT_SECRET

# Frontend Configuration
VITE_API_BASE_URL=$VITE_API_BASE_URL
VITE_GATEWAY_URL=$VITE_GATEWAY_URL

============================================
COMANDOS PARA COPIAR AL EC2
============================================

# Conectarse al EC2
ssh -i $SSH_KEY_PATH $EC2_USER@$EC2_HOST

# Crear archivo .env
cd $DEPLOY_PATH
nano .env
# (pegar el contenido de arriba)

# Dar permisos al script de deploy
chmod +x scripts/deploy.sh

# Probar el deployment
bash scripts/deploy.sh

============================================
PASOS SIGUIENTES
============================================

1. Configurar todos los secrets en GitHub:
   Settings → Secrets and variables → Actions

2. Copiar el archivo .env al EC2

3. Configurar Git en el EC2 para pull automático:
   - Opción A: Personal Access Token
   - Opción B: Deploy Key

   Ver: .github/CICD_SETUP.md

4. Hacer un push a main para probar el CI/CD

============================================
EOF

echo -e "${GREEN}✓ Archivo generado: ${SECRETS_FILE}${NC}"
echo ""
echo -e "${YELLOW}Este archivo contiene:${NC}"
echo "  1. Todos los valores para configurar en GitHub Secrets"
echo "  2. El contenido del archivo .env para el EC2"
echo "  3. Comandos útiles para la configuración"
echo ""
echo -e "${CYAN}Pasos siguientes:${NC}"
echo "  1. Abre el archivo: ${SECRETS_FILE}"
echo "  2. Configura los secrets en GitHub"
echo "  3. Copia el .env al EC2"
echo "  4. Lee la guía completa: .github/CICD_SETUP.md"
echo ""
echo -e "${RED}⚠️  IMPORTANTE:${NC}"
echo "  - Este archivo contiene información sensible"
echo "  - Elimínalo después de usarlo: rm ${SECRETS_FILE}"
echo "  - NO lo subas a Git (ya está en .gitignore)"
echo ""
echo -e "${BLUE}============================================${NC}"

# Agregar el archivo al .gitignore si no está
if ! grep -q "github-secrets-.*\.txt" .gitignore 2>/dev/null; then
    echo "github-secrets-*.txt" >> .gitignore
    echo -e "${GREEN}✓ Agregado al .gitignore${NC}"
fi
