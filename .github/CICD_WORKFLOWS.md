# CI/CD Workflows - AgroMercado

Este documento describe los workflows de GitHub Actions utilizados para automatizar el despliegue, monitoreo y rollback de AgroMercado.

## 📋 Workflows Disponibles

### 1. 🚀 Deploy to AWS EC2 (`deploy.yml`)

**Trigger**: Automático cuando se mergea un PR a `main` o se hace push directo a `main`

**Función**: Despliega la aplicación completa en EC2:
- Actualiza el código desde GitHub
- Reconstruye las imágenes Docker
- Reinicia todos los contenedores
- Ejecuta health checks
- Notifica el resultado

**Duración**: ~15-20 minutos

**Cómo usar**:
1. Crea un PR hacia `main`
2. Mergea el PR
3. El deployment se ejecuta automáticamente
4. Ve a Actions para ver el progreso

---

### 2. 🏥 Health Check (`health-check.yml`)

**Trigger**: Manual o programado (opcional)

**Función**: Verifica que todos los servicios estén funcionando:
- Frontend (puerto 80)
- API Gateway (puerto 8080)
- Eureka Server (puerto 8761)
- RabbitMQ Management (puerto 15672)
- Registro de servicios en Eureka

**Duración**: ~1-2 minutos

**Cómo usar**:
1. Ve a Actions → Health Check
2. Click "Run workflow"
3. Selecciona la rama (normalmente `main`)
4. Click "Run workflow"

**Programar ejecución automática**:
Descomenta la línea de `schedule` en el workflow para que se ejecute cada 6 horas.

---

### 3. 🔄 Rollback Deployment (`rollback.yml`)

**Trigger**: Manual

**Función**: Revierte la aplicación a una versión anterior:
- Hace rollback a un commit específico
- Reconstruye y reinicia los contenedores
- Verifica que los servicios funcionen

**Duración**: ~10-15 minutos

**Cómo usar**:
1. Ve a Actions → Rollback Deployment
2. Click "Run workflow"
3. (Opcional) Ingresa el commit SHA al que quieres volver
   - Deja vacío para volver al commit anterior (`HEAD~1`)
4. Click "Run workflow"

**Ejemplos**:
```bash
# Rollback al commit anterior
Deja el campo vacío o escribe: HEAD~1

# Rollback a 3 commits atrás
HEAD~3

# Rollback a un commit específico
abc123def456
```

---

## 🔐 Configuración Requerida

Antes de usar estos workflows, debes configurar los siguientes **Secrets** en GitHub:

### Secrets de Conexión EC2
- `EC2_SSH_PRIVATE_KEY` - Clave privada SSH completa
- `EC2_HOST` - IP pública del EC2
- `EC2_USER` - Usuario SSH (normalmente `ubuntu`)
- `DEPLOY_PATH` - Ruta del código en EC2 (ej: `/home/ubuntu/agromercado`)

### Secrets de Aplicación
- `POSTGRES_PASSWORD` - Password de PostgreSQL
- `MONGO_ROOT_PASSWORD` - Password de MongoDB
- `RABBITMQ_PASS` - Password de RabbitMQ
- `JWT_SECRET` - Secret key para JWT
- `VITE_API_BASE_URL` - URL del API Gateway
- `VITE_GATEWAY_URL` - URL del Gateway

📚 **Guía completa**: Ver [CICD_SETUP.md](./CICD_SETUP.md)

---

## 🎯 Flujo de Trabajo Recomendado

### Desarrollo Normal

1. **Crear una rama de feature**
```bash
git checkout -b feature/nueva-funcionalidad
```

2. **Hacer cambios y commit**
```bash
git add .
git commit -m "feat: agregar nueva funcionalidad"
git push origin feature/nueva-funcionalidad
```

3. **Crear Pull Request**
- Ve a GitHub
- Crea PR hacia `main`
- Espera revisión del código

4. **Mergear PR**
- Una vez aprobado, mergea el PR
- El workflow `deploy.yml` se ejecuta automáticamente
- Ve a Actions para monitorear el progreso

5. **Verificar deployment**
- Espera ~15-20 minutos
- Verifica que el workflow termine exitosamente
- Prueba la aplicación en: `http://TU_IP_EC2`

### En Caso de Problemas

Si algo sale mal después del deployment:

1. **Ejecutar Health Check**
```
Actions → Health Check → Run workflow
```

2. **Ver logs en el EC2**
```bash
ssh -i ~/.ssh/tu-clave.pem ubuntu@TU_IP_EC2
cd /home/ubuntu/agromercado
docker-compose logs -f
```

3. **Hacer Rollback si es necesario**
```
Actions → Rollback Deployment → Run workflow
```

---

## 📊 Monitoreo

### Ver Estado de Workflows

1. Ve a la pestaña **Actions** en GitHub
2. Verás una lista de todos los workflows ejecutados
3. Click en cualquiera para ver detalles:
   - ✅ Verde = Exitoso
   - ❌ Rojo = Falló
   - 🟡 Amarillo = En progreso

### Ver Logs Detallados

1. Click en un workflow
2. Click en el job (ej: "Deploy to EC2")
3. Expande cada step para ver los logs

### Badges de Estado (Opcional)

Agrega esto al README.md principal para mostrar el estado:

```markdown
![Deploy Status](https://github.com/TU_USUARIO/TU_REPO/workflows/Deploy%20to%20AWS%20EC2/badge.svg)
```

---

## 🐛 Troubleshooting

### Workflow falla en "Configure SSH"

**Causa**: Clave SSH mal configurada

**Solución**: Verifica que `EC2_SSH_PRIVATE_KEY` incluya las líneas BEGIN/END completas

### Workflow falla en "Deploy to EC2"

**Causa**: No puede conectarse al EC2 o el código no se actualiza

**Solución**:
1. Verifica Security Group del EC2 (debe permitir SSH)
2. Verifica que Git esté configurado correctamente en EC2
3. Verifica el secret `DEPLOY_PATH`

### Workflow falla en "Health Check"

**Causa**: Los servicios no arrancaron a tiempo

**Solución**: Aumenta el tiempo de espera en el workflow (edita el `sleep`)

### Los contenedores no se actualizan

**Causa**: Docker usa caché de imágenes antiguas

**Solución**: El script ya incluye `--no-cache`, pero puedes forzar limpieza:
```bash
ssh -i ~/.ssh/tu-clave.pem ubuntu@TU_IP_EC2
cd /home/ubuntu/agromercado
docker-compose down -v
docker system prune -af
docker-compose up -d --build
```

---

## 🚀 Mejoras Futuras

Ideas para extender el CI/CD:

- [ ] Agregar tests automáticos antes del deploy
- [ ] Notificaciones a Slack/Discord
- [ ] Deployment a múltiples environments (staging/production)
- [ ] Aprobación manual antes del deploy a producción
- [ ] Integración con herramientas de monitoreo (DataDog, New Relic)
- [ ] Rollback automático si el health check falla

---

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Documentation](https://docs.docker.com/)
- [Deployment Guide](../DEPLOYMENT.md)
- [CI/CD Setup Guide](./CICD_SETUP.md)

---

¿Necesitas ayuda? Revisa la [Guía de Setup de CI/CD](./CICD_SETUP.md) o abre un issue.
