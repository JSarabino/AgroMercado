#!/bin/bash

# ============================================
# Script para Configuración Inicial del EC2
# ============================================
# Ejecuta este script EN EL EC2 después de la
# instalación de la infraestructura con Pulumi
# ============================================

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo "============================================"
log_info "Configuración del EC2 para CI/CD"
echo "============================================"
echo ""

# Verificar que estamos en EC2
if [ ! -f /etc/cloud/cloud.cfg ]; then
    log_warning "No parece que estés en una instancia EC2"
    read -p "¿Continuar de todas formas? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# ============================================
# 1. Actualizar sistema
# ============================================
log_info "Actualizando sistema operativo..."
sudo apt-get update -qq
sudo apt-get upgrade -y -qq

# ============================================
# 2. Instalar dependencias
# ============================================
log_info "Instalando dependencias necesarias..."

# Git
if ! command -v git &> /dev/null; then
    log_info "Instalando Git..."
    sudo apt-get install -y git
fi

# Docker
if ! command -v docker &> /dev/null; then
    log_info "Instalando Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    log_success "Docker instalado"
else
    log_success "Docker ya está instalado"
fi

# Docker Compose
if ! command -v docker-compose &> /dev/null; then
    log_info "Instalando Docker Compose..."
    sudo apt-get install -y docker-compose-plugin
    log_success "Docker Compose instalado"
else
    log_success "Docker Compose ya está instalado"
fi

# Herramientas útiles
log_info "Instalando herramientas útiles..."
sudo apt-get install -y curl wget nano htop jq

# ============================================
# 3. Configurar Git
# ============================================
echo ""
log_info "Configuración de Git"
echo ""

read -p "¿Tu repositorio es privado? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Para repositorios privados, tienes 2 opciones:"
    echo "  1. Personal Access Token (Recomendado)"
    echo "  2. Deploy Key"
    echo ""
    read -p "¿Qué opción prefieres? (1/2): " -n 1 -r
    echo ""

    if [[ $REPLY == "1" ]]; then
        echo ""
        log_info "Configuración con Personal Access Token"
        echo ""
        echo "1. Ve a GitHub: Settings → Developer settings → Personal access tokens"
        echo "2. Generate new token (classic)"
        echo "3. Selecciona scope: 'repo' (Full control of private repositories)"
        echo "4. Copia el token"
        echo ""
        read -p "Pega tu Personal Access Token: " -s GITHUB_TOKEN
        echo ""

        read -p "URL del repositorio (ej: github.com/user/repo): " REPO_URL

        # Configurar remote con token
        cd $HOME/agromercado
        git remote set-url origin "https://${GITHUB_TOKEN}@${REPO_URL}.git"

        log_success "Remote configurado con Personal Access Token"

    elif [[ $REPLY == "2" ]]; then
        echo ""
        log_info "Configuración con Deploy Key"
        echo ""

        # Generar SSH key
        SSH_KEY_PATH="$HOME/.ssh/github_deploy"
        if [ ! -f "$SSH_KEY_PATH" ]; then
            ssh-keygen -t ed25519 -C "github-deploy" -f "$SSH_KEY_PATH" -N ""
            log_success "SSH key generada"
        fi

        # Configurar SSH config
        cat >> $HOME/.ssh/config << EOF

Host github.com
    HostName github.com
    User git
    IdentityFile $SSH_KEY_PATH
EOF
        chmod 600 $HOME/.ssh/config

        echo ""
        log_info "Copia esta clave pública y agrégala a GitHub:"
        echo ""
        cat "${SSH_KEY_PATH}.pub"
        echo ""
        echo "Ve a: Settings → Deploy keys → Add deploy key"
        echo "✓ Marca 'Allow write access'"
        echo ""
        read -p "Presiona Enter cuando hayas agregado la clave..."

        read -p "URL del repositorio SSH (ej: git@github.com:user/repo.git): " REPO_SSH_URL

        cd $HOME/agromercado
        git remote set-url origin "$REPO_SSH_URL"

        log_success "Remote configurado con Deploy Key"
    fi

    # Probar conexión
    echo ""
    log_info "Probando conexión con GitHub..."
    if git ls-remote &> /dev/null; then
        log_success "✓ Conexión exitosa con GitHub"
    else
        log_error "✗ No se pudo conectar con GitHub. Verifica la configuración."
        exit 1
    fi
else
    log_info "Repositorio público, no se necesita autenticación adicional"
fi

# ============================================
# 4. Clonar o actualizar repositorio
# ============================================
echo ""
log_info "Configurando repositorio"
echo ""

REPO_DIR="$HOME/agromercado"

if [ ! -d "$REPO_DIR" ]; then
    read -p "URL del repositorio para clonar: " CLONE_URL
    git clone "$CLONE_URL" "$REPO_DIR"
    log_success "Repositorio clonado en $REPO_DIR"
else
    log_success "Repositorio ya existe en $REPO_DIR"
    cd "$REPO_DIR"
    git pull
    log_success "Repositorio actualizado"
fi

# ============================================
# 5. Crear archivo .env
# ============================================
echo ""
log_info "Configuración del archivo .env"
echo ""

cd "$REPO_DIR"

if [ -f ".env" ]; then
    log_warning "El archivo .env ya existe"
    read -p "¿Quieres sobrescribirlo? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Conservando .env existente"
    else
        rm .env
    fi
fi

if [ ! -f ".env" ]; then
    echo ""
    log_info "Necesitas proporcionar los valores para el archivo .env"
    echo ""

    read -p "IP pública de este EC2: " PUBLIC_IP
    read -p "PostgreSQL password: " POSTGRES_PASS
    read -p "MongoDB password: " MONGO_PASS
    read -p "RabbitMQ password: " RABBIT_PASS
    read -p "JWT Secret: " JWT_SECRET

    cat > .env << EOF
SPRING_PROFILES_ACTIVE=prod

# Database - PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=${POSTGRES_PASS}
POSTGRES_PORT=5432

# Database - MongoDB
MONGO_ROOT_USER=root
MONGO_ROOT_PASSWORD=${MONGO_PASS}
MONGO_PORT=27017

# Message Broker - RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=${RABBIT_PASS}
RABBITMQ_PORT=5672
RABBITMQ_MGMT_PORT=15672

# Microservices Ports
API_GATEWAY_PORT=8080
FRONTEND_PORT=80

# Security
JWT_SECRET=${JWT_SECRET}

# Frontend Configuration
VITE_API_BASE_URL=http://${PUBLIC_IP}:8080
VITE_GATEWAY_URL=http://${PUBLIC_IP}:8080
EOF

    log_success "Archivo .env creado"
fi

# ============================================
# 6. Permisos de scripts
# ============================================
log_info "Configurando permisos de scripts..."
chmod +x scripts/*.sh
log_success "Permisos configurados"

# ============================================
# 7. Iniciar Docker si no está corriendo
# ============================================
if ! docker ps &> /dev/null; then
    log_info "Iniciando Docker..."
    sudo systemctl start docker
    sudo systemctl enable docker
fi

# ============================================
# 8. Pull de imágenes base (opcional)
# ============================================
echo ""
read -p "¿Quieres hacer pull de las imágenes base ahora? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    log_info "Descargando imágenes base..."
    docker-compose pull postgres mongodb rabbitmq
    log_success "Imágenes descargadas"
fi

# ============================================
# 9. Resumen
# ============================================
echo ""
echo "============================================"
log_success "Configuración completada"
echo "============================================"
echo ""
log_info "Resumen:"
echo "  ✓ Sistema actualizado"
echo "  ✓ Docker y Docker Compose instalados"
echo "  ✓ Repositorio configurado en $REPO_DIR"
echo "  ✓ Git configurado para CI/CD"
echo "  ✓ Archivo .env creado"
echo "  ✓ Scripts con permisos correctos"
echo ""
log_info "Siguiente paso:"
echo "  1. Verifica el archivo .env: nano $REPO_DIR/.env"
echo "  2. Prueba el deployment: cd $REPO_DIR && bash scripts/deploy.sh"
echo "  3. Configura los GitHub Secrets (ver .github/CICD_SETUP.md)"
echo ""
log_warning "IMPORTANTE:"
echo "  Si instalaste Docker por primera vez, cierra y vuelve a abrir la sesión SSH"
echo "  para que los permisos de Docker se apliquen correctamente."
echo ""
echo "============================================"
