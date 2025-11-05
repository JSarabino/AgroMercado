# Configuración de CI/CD con GitHub Actions

Este documento te guiará para configurar el pipeline de CI/CD que despliega automáticamente tu aplicación en AWS EC2 cuando se mergea un Pull Request a la rama `main`.

## 📋 Tabla de Contenidos

1. [Cómo Funciona](#cómo-funciona)
2. [Prerequisitos](#prerequisitos)
3. [Configuración de Secrets en GitHub](#configuración-de-secrets-en-github)
4. [Configuración del EC2](#configuración-del-ec2)
5. [Probar el Pipeline](#probar-el-pipeline)
6. [Troubleshooting](#troubleshooting)

## 🔄 Cómo Funciona

El workflow de GitHub Actions se activa automáticamente cuando:
- Se hace un **push directo** a la rama `main`
- Se **mergea un Pull Request** a la rama `main`

El proceso de deployment:
1. ✅ Checkout del código
2. 🔐 Configura SSH para conectarse al EC2
3. 📥 Actualiza el código en el EC2 (git pull)
4. ⚙️ Actualiza las variables de entorno
5. 🐳 Reconstruye y reinicia los contenedores Docker
6. 🔍 Ejecuta health checks
7. ✉️ Notifica el resultado

## 📦 Prerequisitos

### 1. Instancia EC2 Configurada

Debes tener:
- ✅ EC2 corriendo con Docker y Docker Compose instalados
- ✅ Repositorio clonado en el EC2
- ✅ Acceso SSH configurado

Si aún no lo has hecho, sigue la [Guía de Deployment](../../DEPLOYMENT.md).

### 2. Clave SSH

Necesitas la **clave privada SSH** (archivo `.pem`) que usas para conectarte al EC2.

```bash
# En tu máquina local, obtén el contenido de la clave
cat ~/.ssh/tu-clave-ec2.pem
```

⚠️ **IMPORTANTE**: Guarda todo el contenido, incluyendo las líneas `-----BEGIN RSA PRIVATE KEY-----` y `-----END RSA PRIVATE KEY-----`

### 3. IP Pública del EC2

```bash
# Obtener la IP pública desde Pulumi
cd infrastructure/pulumi
pulumi stack output publicIp

# O desde AWS CLI
aws ec2 describe-instances --instance-ids YOUR_INSTANCE_ID --query 'Reservations[0].Instances[0].PublicIpAddress'
```

## 🔐 Configuración de Secrets en GitHub

### Paso 1: Acceder a la Configuración de Secrets

1. Ve a tu repositorio en GitHub
2. Click en **Settings** (Configuración)
3. En el menú lateral, ve a **Secrets and variables** → **Actions**
4. Click en **New repository secret**

### Paso 2: Agregar los Secrets

Necesitas crear los siguientes secrets. Para cada uno:
1. Click **New repository secret**
2. Ingresa el **Name** (nombre exacto como se indica abajo)
3. Pega el **Value** (valor)
4. Click **Add secret**

#### Secrets de Conexión EC2

| Secret Name | Descripción | Ejemplo |
|-------------|-------------|---------|
| `EC2_SSH_PRIVATE_KEY` | Tu clave privada SSH completa | El contenido completo de tu archivo `.pem` |
| `EC2_HOST` | IP pública de tu EC2 | `3.18.218.122` |
| `EC2_USER` | Usuario SSH (normalmente `ubuntu`) | `ubuntu` |
| `DEPLOY_PATH` | Ruta donde está el código en EC2 | `/home/ubuntu/agromercado` |

**Ejemplo de `EC2_SSH_PRIVATE_KEY`:**
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
...contenido de tu clave...
...múltiples líneas...
-----END RSA PRIVATE KEY-----
```

#### Secrets de Base de Datos y Servicios

| Secret Name | Descripción | Ejemplo |
|-------------|-------------|---------|
| `POSTGRES_PASSWORD` | Password de PostgreSQL | `AgroMercadoProd2024!` |
| `MONGO_ROOT_PASSWORD` | Password de MongoDB | `AgroMercadoProd2024!` |
| `RABBITMQ_PASS` | Password de RabbitMQ | `AgroMercadoProd2024!` |
| `JWT_SECRET` | Secret key para JWT | `Taknfw2le9O8Y7Taknfw2le9O8Y7` |

#### Secrets de Frontend

| Secret Name | Descripción | Ejemplo |
|-------------|-------------|---------|
| `VITE_API_BASE_URL` | URL del API Gateway | `http://3.18.218.122:8080` |
| `VITE_GATEWAY_URL` | URL del Gateway | `http://3.18.218.122:8080` |

### Paso 3: Verificar los Secrets

Una vez agregados todos los secrets, deberías ver una lista como esta:

```
✅ EC2_SSH_PRIVATE_KEY
✅ EC2_HOST
✅ EC2_USER
✅ DEPLOY_PATH
✅ POSTGRES_PASSWORD
✅ MONGO_ROOT_PASSWORD
✅ RABBITMQ_PASS
✅ JWT_SECRET
✅ VITE_API_BASE_URL
✅ VITE_GATEWAY_URL
```

## ⚙️ Configuración del EC2

### 1. Asegurarte de que el repositorio esté clonado

Conéctate a tu EC2:
```bash
ssh -i ~/.ssh/tu-clave.pem ubuntu@TU_IP_EC2
```

Verifica que el código esté en la ruta correcta:
```bash
ls -la /home/ubuntu/agromercado
```

Si no está clonado:
```bash
cd /home/ubuntu
git clone https://github.com/TU_USUARIO/TU_REPO.git agromercado
cd agromercado
```

### 2. Configurar autenticación de Git (para repositorios privados)

Si tu repositorio es privado, necesitas configurar la autenticación:

**Opción A: Personal Access Token (Recomendado)**

1. Crea un Personal Access Token en GitHub:
   - Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token
   - Selecciona scope: `repo` (Full control of private repositories)
   - Copia el token

2. Configura Git en el EC2:
```bash
cd /home/ubuntu/agromercado

# Configurar el remote con el token
git remote set-url origin https://TU_TOKEN@github.com/TU_USUARIO/TU_REPO.git

# Verificar
git remote -v
```

**Opción B: Deploy Key (Alternativa)**

1. Genera una clave SSH en el EC2:
```bash
ssh-keygen -t ed25519 -C "github-deploy" -f ~/.ssh/github_deploy
cat ~/.ssh/github_deploy.pub
```

2. Agrega la clave pública a GitHub:
   - Settings → Deploy keys → Add deploy key
   - Pega la clave pública
   - ✅ Marca "Allow write access"

3. Configura Git:
```bash
# Agregar a ~/.ssh/config
cat >> ~/.ssh/config << EOF
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/github_deploy
EOF

# Cambiar remote a SSH
cd /home/ubuntu/agromercado
git remote set-url origin git@github.com:TU_USUARIO/TU_REPO.git
```

### 3. Probar que Git puede hacer pull

```bash
cd /home/ubuntu/agromercado
git pull origin main
```

Debe funcionar sin pedir contraseña.

### 4. Dar permisos al script de deploy

```bash
chmod +x /home/ubuntu/agromercado/scripts/deploy.sh
```

## 🧪 Probar el Pipeline

### 1. Crear una rama y hacer un cambio

```bash
# En tu máquina local
git checkout -b test-cicd

# Hacer un cambio pequeño (ejemplo)
echo "# CI/CD Test" >> README.md

git add README.md
git commit -m "test: verificar pipeline de CI/CD"
git push origin test-cicd
```

### 2. Crear un Pull Request

1. Ve a tu repositorio en GitHub
2. Verás un banner "Compare & pull request"
3. Click en ese botón
4. Completa la descripción
5. Click "Create pull request"

### 3. Mergear el PR

1. Revisa el código
2. Click "Merge pull request"
3. Click "Confirm merge"

### 4. Ver el Deployment

1. Ve a la pestaña **Actions** en GitHub
2. Verás el workflow "Deploy to AWS EC2" ejecutándose
3. Click en el workflow para ver los logs en tiempo real

El proceso tomará aproximadamente 15-20 minutos la primera vez.

### 5. Verificar el Deployment

Una vez completado:

```bash
# Frontend
curl http://TU_IP_EC2

# API Gateway Health
curl http://TU_IP_EC2:8080/actuator/health

# Eureka Dashboard
# Abre en el navegador: http://TU_IP_EC2:8761
```

## 🐛 Troubleshooting

### Error: "Permission denied (publickey)"

**Causa**: La clave SSH no está configurada correctamente.

**Solución**:
1. Verifica que el secret `EC2_SSH_PRIVATE_KEY` tenga el contenido completo de la clave
2. Asegúrate de que incluye las líneas `-----BEGIN/END-----`
3. Verifica que el Security Group del EC2 permita SSH desde GitHub Actions

### Error: "Host key verification failed"

**Causa**: El EC2 no está en known_hosts.

**Solución**: El workflow ya incluye `ssh-keyscan`, pero puedes verificar manualmente:
```bash
ssh-keyscan -H TU_IP_EC2 >> ~/.ssh/known_hosts
```

### Error: "fatal: could not read Username"

**Causa**: Git no puede autenticarse para hacer pull.

**Solución**: Configura el Personal Access Token o Deploy Key como se indica arriba.

### Error: "docker-compose: command not found"

**Causa**: Docker Compose no está instalado en el EC2.

**Solución**:
```bash
ssh -i ~/.ssh/tu-clave.pem ubuntu@TU_IP_EC2
sudo apt-get update
sudo apt-get install docker-compose-plugin
```

### Los servicios no arrancan después del deploy

**Verificar logs**:
```bash
ssh -i ~/.ssh/tu-clave.pem ubuntu@TU_IP_EC2
cd /home/ubuntu/agromercado
docker-compose logs -f
```

**Verificar recursos**:
```bash
free -h  # Memoria
df -h    # Disco
docker stats  # Uso de recursos por contenedor
```

### El health check falla

**Aumentar el tiempo de espera**:
Edita `.github/workflows/deploy.yml` y aumenta el sleep después de `docker-compose up`:
```yaml
sleep 120  # De 60 a 120 segundos
```

### Ver logs del workflow

1. GitHub Actions → Click en el workflow fallido
2. Click en el job "Deploy to EC2"
3. Expande cada step para ver los logs detallados

## 📊 Monitorear Deployments

### Ver historial de deployments

1. Ve a **Actions** en GitHub
2. Verás todos los workflows ejecutados
3. Click en cualquiera para ver detalles

### Configurar notificaciones

GitHub enviará notificaciones por email si:
- El deployment falla
- Puedes configurar Slack/Discord con webhooks

### Ejemplo de notificación a Slack

Agrega al final del workflow:

```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Deployment to EC2 completed'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 🔄 Actualizar el Pipeline

Para hacer cambios al pipeline:

1. Edita `.github/workflows/deploy.yml`
2. Commit y push los cambios
3. El nuevo workflow se usará en el siguiente deploy

## 🎯 Mejores Prácticas

### 1. Usar Environments en GitHub

Configura environments para staging/production:
```yaml
jobs:
  deploy:
    environment: production
    # ... resto del job
```

### 2. Agregar tests antes del deploy

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run tests
        run: |
          cd Backend/accounts-service
          ./mvnw test

  deploy:
    needs: test
    # ... resto del job
```

### 3. Manual Approval

Requiere aprobación manual antes del deploy:
```yaml
jobs:
  deploy:
    environment: production  # Configura protection rules en Settings
```

### 4. Rollback automático

Si el health check falla, hacer rollback:
```yaml
- name: Rollback on failure
  if: failure()
  run: |
    ssh -i ~/.ssh/deploy_key $EC2_USER@$EC2_HOST << 'ENDSSH'
      cd $DEPLOY_PATH
      git reset --hard HEAD~1
      docker-compose up -d
    ENDSSH
```

## 📚 Recursos Adicionales

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)

## 🆘 Soporte

Si tienes problemas:
1. Revisa los logs en GitHub Actions
2. Conéctate al EC2 y revisa logs: `docker-compose logs -f`
3. Verifica que todos los secrets estén configurados
4. Abre un issue en el repositorio

---

¡Listo! Ahora cada vez que mergees un PR a `main`, tu aplicación se desplegará automáticamente en AWS EC2. 🚀
