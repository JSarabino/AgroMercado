# AgroMercado - Guía de Despliegue en AWS

Esta guía te ayudará a desplegar AgroMercado en AWS EC2 usando Pulumi y Docker.

## 📋 Tabla de Contenidos

1. [Arquitectura](#arquitectura)
2. [Prerequisitos](#prerequisitos)
3. [Configuración Local](#configuración-local)
4. [Despliegue con Pulumi](#despliegue-con-pulumi)
5. [Configuración de la Aplicación](#configuración-de-la-aplicación)
6. [Despliegue de los Servicios](#despliegue-de-los-servicios)
7. [Verificación](#verificación)
8. [Mantenimiento](#mantenimiento)
9. [Troubleshooting](#troubleshooting)

## 🏗️ Arquitectura

### Infraestructura
- **EC2 Instance**: t3.xlarge (4 vCPU, 16 GB RAM)
- **VPC**: Red privada virtual con subnet pública
- **Security Groups**: Configurados para HTTP (80), API (8080), SSH (22)
- **Elastic IP**: IP pública estática

### Aplicación
- **Frontend**: React + Vite (Nginx)
- **API Gateway**: Spring Cloud Gateway (Puerto 8080)
- **Service Discovery**: Eureka Server (Puerto 8761)
- **Microservicios**:
  - Accounts Service (Puerto 8081)
  - Productos Service (Puerto 5001)
  - Pedidos Service (Porto 5003)
- **Base de Datos**:
  - PostgreSQL (Puerto 5432)
  - MongoDB (Puerto 27017)
- **Message Broker**: RabbitMQ (Puerto 5672, Management 15672)

## 📦 Prerequisitos

### En tu máquina local

1. **AWS CLI**
```bash
# Windows
choco install awscli

# MacOS
brew install awscli

# Linux
sudo apt-get install awscli
```

2. **Pulumi CLI**
```bash
# Windows
choco install pulumi

# MacOS
brew install pulumi

# Linux
curl -fsSL https://get.pulumi.com | sh
```

3. **Node.js** (v18+)
```bash
# Verificar instalación
node --version
npm --version
```

4. **Git**
```bash
git --version
```

### En AWS

1. **Cuenta AWS** activa
2. **IAM User** con permisos de:
   - EC2 (create, modify, delete)
   - VPC (create, modify)
   - SecurityGroups (create, modify)

3. **SSH Key Pair** creado en EC2
   - Ve a EC2 Console → Key Pairs → Create Key Pair
   - Descarga el archivo `.pem`
   - Guárdalo en `~/.ssh/` con permisos `chmod 400`

## 🔧 Configuración Local

### 1. Clonar el Repositorio

```bash
git clone <your-repo-url>
cd AgroMercado
```

### 2. Configurar AWS CLI

```bash
aws configure
```

Ingresa:
- AWS Access Key ID
- AWS Secret Access Key
- Default region: `us-east-1`
- Default output format: `json`

Verificar:
```bash
aws sts get-caller-identity
```

### 3. Preparar Variables de Entorno

```bash
# Copiar template
cp env.example .env

# Editar con tus valores (se configurará más adelante con la IP de EC2)
```

## 🚀 Despliegue con Pulumi

### 1. Instalar Dependencias

```bash
cd infrastructure/pulumi
npm install
```

### 2. Configurar Pulumi

```bash
# Login (usar backend local o Pulumi Cloud)
pulumi login --local

# Crear stack
pulumi stack init production

# Configurar variables
pulumi config set keyName YOUR_AWS_KEY_NAME
pulumi config set instanceType t3.xlarge
pulumi config set allowedSSHIP 8.242.189.0/24  # Tu IP pública
pulumi config set aws:region us-east-2
```

### 3. Previsualizar Infraestructura

```bash
pulumi preview
```

### 4. Desplegar Infraestructura

```bash
pulumi up
```

Confirma con "yes". El proceso tomará ~5-10 minutos.

### 5. Obtener Outputs

```bash
pulumi stack output
```

Guarda estos valores:
- `publicIp`: La IP pública de tu instancia EC2
- `frontendUrl`: URL del frontend
- `apiGatewayUrl`: URL del API Gateway
- `sshCommand`: Comando para conectarte por SSH

**Ejemplo de output:**
```
Outputs:
  apiGatewayUrl     : "http://52.23.45.67:8080"
  envVariables      : "VITE_API_BASE_URL=http://52.23.45.67:8080..."
  eurekaUrl         : "http://52.23.45.67:8761"
  frontendUrl       : "http://52.23.45.67"
  instanceId        : "i-0123456789abcdef"
  publicIp          : "52.23.45.67"
  sshCommand        : "ssh -i ~/.ssh/my-key.pem ubuntu@52.23.45.67"
```

## ⚙️ Configuración de la Aplicación

### 1. Conectarse a EC2

```bash
# Usar el comando del output de Pulumi
ssh -i ~/.ssh/your-key.pem ubuntu@<PUBLIC_IP>
```

### 2. Clonar el Repositorio

```bash
cd /home/ubuntu/agromercado
git clone <your-repo-url> .
```

Si el repositorio es privado:
```bash
# Opción 1: HTTPS con token
git clone https://<TOKEN>@github.com/user/repo.git .

# Opción 2: SSH (configurar SSH key en GitHub)
git clone git@github.com:user/repo.git .
```

### 3. Crear Archivo .env

```bash
nano .env
```

Pega lo siguiente, reemplazando `<PUBLIC_IP>` con tu IP de EC2:

```bash
SPRING_PROFILES_ACTIVE=prod

# Database - PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=AgroMercadoProd2024!
POSTGRES_PORT=5432

# Database - MongoDB
MONGO_ROOT_USER=root
MONGO_ROOT_PASSWORD=AgroMercadoProd2024!
MONGO_PORT=27017

# Message Broker - RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=AgroMercadoProd2024!
RABBITMQ_PORT=5672
RABBITMQ_MGMT_PORT=15672

# Microservices Ports
API_GATEWAY_PORT=8080
FRONTEND_PORT=80

# Security
JWT_SECRET=prod-secure-jwt-key-0123456789abcdefghijklmnopqrstuvwxyz

# Frontend Configuration
VITE_API_BASE_URL=http://<PUBLIC_IP>:8080
VITE_GATEWAY_URL=http://<PUBLIC_IP>:8080
```

Guardar: `Ctrl+X`, `Y`, `Enter`

## 🐳 Despliegue de los Servicios

### Opción 1: Usar el Script de Despliegue

```bash
bash /home/ubuntu/deploy.sh
```

### Opción 2: Manual

```bash
cd /home/ubuntu/agromercado

# Construir y levantar todos los servicios
docker-compose up -d --build

# Ver el progreso
docker-compose logs -f
```

**Nota**: El primer build tomará 15-20 minutos porque debe:
1. Compilar todos los microservicios Java
2. Construir el frontend de React
3. Crear todas las imágenes Docker

### Verificar Estado

```bash
# Ver contenedores corriendo
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f api-gateway
docker-compose logs -f frontend
```

## ✅ Verificación

### 1. Verificar Servicios de Infraestructura

```bash
# PostgreSQL
docker-compose exec postgres psql -U postgres -c "SELECT version();"

# MongoDB
docker-compose exec mongodb mongosh -u root -p root --eval "db.version()"

# RabbitMQ
curl http://localhost:15672
```

### 2. Verificar Microservicios

Desde tu navegador o terminal local:

```bash
# API Gateway Health
curl http://<PUBLIC_IP>:8080/actuator/health

# Eureka Server (ver servicios registrados)
curl http://<PUBLIC_IP>:8761/

# Frontend
curl http://<PUBLIC_IP>
```

### 3. Probar la Aplicación

1. **Frontend**: Abre `http://<PUBLIC_IP>` en tu navegador
2. **API Gateway**: Prueba `http://<PUBLIC_IP>:8080/actuator/health`
3. **Eureka Dashboard**: `http://<PUBLIC_IP>:8761`
4. **RabbitMQ Management**: `http://<PUBLIC_IP>:15672` (user: admin, pass: AgroMercadoProd2024!)

## 🔄 Mantenimiento

### Actualizar la Aplicación

```bash
# SSH a EC2
ssh -i ~/.ssh/your-key.pem ubuntu@<PUBLIC_IP>

# Ir al directorio
cd /home/ubuntu/agromercado

# Actualizar código
git pull

# Reconstruir y reiniciar
docker-compose up -d --build

# O usar el script
bash /home/ubuntu/deploy.sh
```

### Ver Logs

```bash
# Todos los servicios
docker-compose logs -f

# Solo errores
docker-compose logs -f | grep -i error

# Servicio específico
docker-compose logs -f accounts-service
```

### Reiniciar Servicios

```bash
# Todos los servicios
docker-compose restart

# Servicio específico
docker-compose restart api-gateway
```

### Limpiar Recursos

```bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (⚠️ ELIMINA DATOS)
docker-compose down -v

# Limpiar imágenes no usadas
docker system prune -a
```

### Backups

**Base de Datos PostgreSQL:**
```bash
# Crear backup
docker-compose exec postgres pg_dumpall -U postgres > backup_$(date +%Y%m%d).sql

# Restaurar backup
cat backup_20240101.sql | docker-compose exec -T postgres psql -U postgres
```

**MongoDB:**
```bash
# Crear backup
docker-compose exec mongodb mongodump --username root --password root --out /data/backup

# Copiar a host
docker cp agromercado-mongodb:/data/backup ./mongo_backup
```

## 🐛 Troubleshooting

### Problema: No puedo conectarme por SSH

**Solución:**
```bash
# Verificar Security Group
aws ec2 describe-security-groups --group-ids <sg-id>

# Verificar permisos del archivo .pem
chmod 400 ~/.ssh/your-key.pem

# Verificar que la instancia esté corriendo
pulumi stack output instanceId
aws ec2 describe-instance-status --instance-ids <instance-id>
```

### Problema: Servicios no arrancan

**Solución:**
```bash
# Ver logs detallados
docker-compose logs

# Verificar recursos
docker stats

# Verificar espacio en disco
df -h

# Reiniciar Docker
sudo systemctl restart docker
docker-compose up -d
```

### Problema: Base de datos no se conecta

**Solución:**
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs postgres

# Verificar conectividad
docker-compose exec postgres pg_isready -U postgres

# Recrear contenedor de base de datos
docker-compose up -d --force-recreate postgres
```

### Problema: Frontend no carga

**Solución:**
```bash
# Verificar logs de Nginx
docker-compose logs frontend

# Verificar que el build se completó
docker-compose exec frontend ls -la /usr/share/nginx/html

# Reconstruir frontend con las URLs correctas
docker-compose up -d --build frontend
```

### Problema: Error de memoria

**Solución:**
```bash
# Verificar uso de memoria
free -h

# Aumentar el tipo de instancia en Pulumi
pulumi config set instanceType t3.2xlarge
pulumi up
```

### Problema: Eureka no muestra servicios

**Solución:**
```bash
# Verificar que Eureka está corriendo
curl http://localhost:8761/eureka/apps

# Reiniciar servicios en orden
docker-compose restart eureka-server
sleep 30
docker-compose restart api-gateway accounts-service productos-service pedidos-service
```

## 📊 Monitoreo

### Comandos Útiles

```bash
# Ver uso de recursos
docker stats

# Ver espacio en disco
df -h

# Ver memoria
free -h

# Ver procesos
top

# Ver conexiones de red
netstat -tulpn
```

### Logs Importantes

```bash
# Sistema
sudo journalctl -u docker -f

# Aplicación
docker-compose logs -f --tail=100

# Filtrar por nivel de error
docker-compose logs | grep -i "error\|exception\|fatal"
```

## 🔐 Seguridad

### Recomendaciones de Producción

1. **Cambiar contraseñas por defecto** en `.env`
2. **Restringir acceso SSH** solo a tu IP
3. **Configurar SSL/TLS** con Let's Encrypt
4. **Habilitar backups automáticos**
5. **Configurar alertas** con CloudWatch
6. **Usar AWS Secrets Manager** para credenciales sensibles
7. **Mantener Docker actualizado**
8. **Actualizar regularmente** las imágenes base

### Configurar HTTPS (Opcional)

```bash
# Instalar Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtener certificado (necesitas un dominio)
sudo certbot --nginx -d yourdomain.com

# El certificado se renovará automáticamente
```

## 💰 Costos Estimados

**Instancia t3.xlarge**:
- Compute: ~$120/mes
- Storage (50GB): ~$5/mes
- Elastic IP: $0 (si está asociada)
- Data Transfer: Variable

**Total aproximado**: $125-150/mes

Para reducir costos:
- Usar t3.large (~$60/mes)
- Detener instancia fuera de horario
- Usar Reserved Instances (descuento hasta 70%)

## 🆘 Soporte

Si necesitas ayuda:
1. Revisa los logs: `docker-compose logs -f`
2. Verifica la documentación de Pulumi
3. Consulta los issues del proyecto
4. Contacta al equipo de desarrollo

## 📚 Referencias

- [Pulumi Documentation](https://www.pulumi.com/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
