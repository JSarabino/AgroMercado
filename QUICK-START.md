# 🚀 AgroMercado - Quick Start Guide

Guía rápida para desplegar AgroMercado en AWS en menos de 30 minutos.

## ⚡ Resumen Ejecutivo

Este proyecto incluye:
- ✅ **5 Microservicios** Spring Boot dockerizados
- ✅ **Frontend React** optimizado con Nginx
- ✅ **Infraestructura como Código** con Pulumi
- ✅ **Auto-despliegue** en AWS EC2
- ✅ **Orquestación** completa con Docker Compose

## 📋 Prerequisitos Rápidos

1. **AWS Account** con permisos de EC2
2. **SSH Key Pair** creado en AWS
3. Instalado: `aws-cli`, `pulumi`, `node.js`, `git`

## 🎯 Despliegue en 5 Pasos

### 1️⃣ Configurar AWS (2 min)

```bash
aws configure
# Ingresa: Access Key, Secret Key, region (us-east-1)
```

### 2️⃣ Desplegar Infraestructura (5 min)

```bash
cd infrastructure/pulumi
npm install

pulumi login --local
pulumi stack init production

# Configura tu SSH key (debe existir en AWS)
pulumi config set keyName YOUR_KEY_NAME
pulumi config set aws:region us-east-1

# Despliega
pulumi up
# Confirma con "yes"
```

**Output esperado:**
```
Outputs:
  frontendUrl: "http://52.23.45.67"
  apiGatewayUrl: "http://52.23.45.67:8080"
  publicIp: "52.23.45.67"
  sshCommand: "ssh -i ~/.ssh/my-key.pem ubuntu@52.23.45.67"
```

### 3️⃣ Conectar a EC2 (1 min)

```bash
# Usar el comando del output
ssh -i ~/.ssh/your-key.pem ubuntu@52.23.45.67
```

### 4️⃣ Clonar y Configurar (3 min)

```bash
# En el servidor EC2
cd /home/ubuntu/agromercado
git clone https://github.com/your-user/agromercado.git .

# Crear archivo .env
nano .env
```

**Pegar esto (reemplaza `<IP>` con tu IP pública):**
```bash
SPRING_PROFILES_ACTIVE=prod
POSTGRES_USER=postgres
POSTGRES_PASSWORD=SecurePass2024!
MONGO_ROOT_USER=root
MONGO_ROOT_PASSWORD=SecurePass2024!
RABBITMQ_USER=admin
RABBITMQ_PASS=SecurePass2024!
JWT_SECRET=prod-secure-jwt-key-0123456789abcdefghijklmnopqrstuvwxyz
VITE_API_BASE_URL=http://<IP>:8080
VITE_GATEWAY_URL=http://<IP>:8080
```

Guardar: `Ctrl+X`, `Y`, `Enter`

### 5️⃣ Desplegar Aplicación (15-20 min)

```bash
# Opción A: Script automático
bash /home/ubuntu/deploy.sh

# Opción B: Manual
docker-compose up -d --build
```

## ✅ Verificar

```bash
# Ver estado
docker-compose ps

# Ver logs
docker-compose logs -f
```

Abrir en el navegador:
- Frontend: `http://<TU-IP>`
- API Gateway: `http://<TU-IP>:8080/actuator/health`
- Eureka: `http://<TU-IP>:8761`

## 🎉 ¡Listo!

Tu aplicación está corriendo en producción.

## 📱 URLs de tu Aplicación

Una vez desplegado, tendrás acceso a:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Frontend** | `http://<IP>` | Aplicación React |
| **API Gateway** | `http://<IP>:8080` | Punto de entrada de APIs |
| **Eureka Dashboard** | `http://<IP>:8761` | Service Discovery |
| **RabbitMQ Management** | `http://<IP>:15672` | Gestión de colas |

**Credenciales RabbitMQ:**
- Usuario: `admin`
- Password: `SecurePass2024!` (o el que configuraste)

## 🔄 Comandos Útiles

```bash
# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar servicios
docker-compose restart

# Actualizar aplicación
git pull && docker-compose up -d --build

# Ver estado de salud
bash scripts/health-check.sh <TU-IP>

# Hacer backup
bash scripts/backup.sh
```

## 🛠️ Troubleshooting Rápido

### No puedo conectar por SSH
```bash
chmod 400 ~/.ssh/your-key.pem
```

### Servicios no arrancan
```bash
# Ver logs
docker-compose logs

# Verificar memoria
free -h

# Reiniciar Docker
sudo systemctl restart docker
docker-compose up -d
```

### Frontend muestra error de conexión
Verifica que las URLs en `.env` tengan la IP pública correcta:
```bash
grep VITE .env
```

### Base de datos no conecta
```bash
# Reiniciar infraestructura
docker-compose restart postgres mongodb rabbitmq

# Esperar 30 segundos
sleep 30

# Reiniciar microservicios
docker-compose restart accounts-service productos-service pedidos-service
```

## 📊 Monitoreo

### Ver recursos
```bash
docker stats
```

### Ver servicios registrados
```bash
curl http://localhost:8761/eureka/apps
```

### Health checks
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:5001/actuator/health
curl http://localhost:5003/actuator/health
```

## 🔒 Seguridad Importante

**Antes de ir a producción:**

1. ✅ Cambia todas las contraseñas en `.env`
2. ✅ Restringe SSH solo a tu IP
3. ✅ Configura HTTPS con SSL
4. ✅ Habilita backups automáticos
5. ✅ Configura alertas

## 💡 Próximos Pasos

1. **Dominio personalizado**: Asocia un dominio a tu IP
2. **HTTPS**: Configura SSL con Let's Encrypt
3. **CI/CD**: Automatiza despliegues con GitHub Actions
4. **Monitoreo**: Integra con CloudWatch o Prometheus
5. **Backups**: Configura snapshots automáticos de EBS

## 📚 Documentación Completa

Para guía detallada, ver:
- [DEPLOYMENT.md](./DEPLOYMENT.md) - Guía completa de despliegue
- [infrastructure/pulumi/README.md](./infrastructure/pulumi/README.md) - Documentación de infraestructura

## 💰 Costos

**Estimado mensual con t3.xlarge:**
- ~$125-150/mes

**Para reducir costos:**
- Usa t3.large (~$60/mes)
- Detén la instancia cuando no la uses
- Usa Reserved Instances (hasta 70% descuento)

## 🆘 Ayuda

Si encuentras problemas:
1. Revisa los logs: `docker-compose logs -f`
2. Ejecuta health check: `bash scripts/health-check.sh localhost`
3. Consulta [DEPLOYMENT.md](./DEPLOYMENT.md)
4. Revisa los issues del repositorio

## 🎓 Arquitectura

```
                        Internet
                           |
                    [Load Balancer]
                           |
              ┌────────────┴────────────┐
              |                         |
         [Frontend]              [API Gateway]
         (Nginx:80)               (8080)
                                        |
                     ┌──────────────────┼──────────────────┐
                     |                  |                  |
              [Accounts]         [Productos]        [Pedidos]
               (8081)              (5001)            (5003)
                     |                  |                  |
              ┌──────┴────────┬─────────┴─────┬───────────┘
              |               |               |
        [PostgreSQL]     [MongoDB]      [RabbitMQ]
         (5432)           (27017)        (5672)
```

## ✨ Características

- 🔄 **Auto-scaling ready**: Preparado para escalamiento horizontal
- 🔍 **Service Discovery**: Eureka para registro automático
- 📦 **Containerized**: Todo en Docker para portabilidad
- 🛡️ **Security**: JWT authentication, CORS configurado
- 📊 **Monitoring**: Actuator endpoints en todos los servicios
- 🗃️ **CQRS**: Arquitectura Command/Query separation
- 🔔 **Event-Driven**: RabbitMQ para mensajería asíncrona
- 💾 **Dual Database**: PostgreSQL + MongoDB

## 🚀 Performance

**Instancia recomendada:** t3.xlarge
- 4 vCPU
- 16 GB RAM
- 50 GB SSD

**Puede manejar:**
- ~1000 usuarios concurrentes
- ~100 req/seg
- ~10K productos

Para más carga, escala horizontalmente o usa t3.2xlarge.

---

**¿Listo para desplegar?** Empieza con el paso 1 ⬆️
