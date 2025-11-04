# 📚 Endpoints Disponibles para Pruebas

## 🎯 Servicios y Puertos

- **Eureka Server**: `http://localhost:8761` (Dashboard)
- **API Gateway**: `http://localhost:8080`
- **accounts-service**: `http://localhost:8081`
- **productos-service**: `http://localhost:5001`

---

## 🔐 Autenticación (Auth Dev)

### 1. Generar Token JWT (Desarrollo)
```http
POST http://localhost:8080/auth/dev/token
Content-Type: application/json

{
  "userId": "USR-ADMIN-GLOBAL",
  "roles": ["ADMIN_GLOBAL"],
  "ttlSeconds": 3600
}
```

**Respuesta:**
```json
{
  "access_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 2. Obtener información del usuario autenticado
```http
GET http://localhost:8080/auth/dev/me
Authorization: Bearer {token}
```

---

## 👤 Usuarios (Command)

### 1. Registrar Usuario (Público)
```http
POST http://localhost:8080/cmd/usuarios
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "nombre": "Juan Pérez"
}
```

**Respuesta:**
```json
{
  "usuarioId": "USR-abc123",
  "email": "usuario@ejemplo.com",
  "nombre": "Juan Pérez",
  "mensaje": "Usuario creado"
}
```

### 2. Agregar Rol Global (Requiere ADMIN_GLOBAL)
```http
PATCH http://localhost:8080/cmd/usuarios/{usuarioId}/roles-globales
Authorization: Bearer {token}
Content-Type: application/json

{
  "rolGlobal": "ADMIN_GLOBAL"
}
```

### 3. Otorgar Membresía Zonal (Requiere ADMIN_GLOBAL)
```http
POST http://localhost:8080/cmd/usuarios/{usuarioId}/membresias
Authorization: Bearer {token}
Content-Type: application/json

{
  "zonaId": "ZONA-001",
  "rolZonal": "ADMIN_ZONA"
}
```

---

## 🏘️ Afiliaciones (Command)

### 1. Solicitar Afiliación (Requiere Autenticación)
```http
POST http://localhost:8080/cmd/afiliaciones/solicitar
Authorization: Bearer {token}
Content-Type: application/json

{
  "zonaId": "ZONA-001",
  "nombreVereda": "Vereda La Esperanza",
  "municipio": "Bogotá",
  "telefono": "3001234567",
  "correo": "contacto@ejemplo.com",
  "representanteNombre": "María García",
  "representanteDocumento": "12345678",
  "representanteCorreo": "maria@ejemplo.com"
}
```

**Respuesta:**
```json
{
  "afiliacionId": "AFIL-xyz789",
  "zonaId": "ZONA-001",
  "mensaje": "Afiliación solicitada"
}
```

### 2. Aprobar Afiliación (Requiere ADMIN_GLOBAL)
```http
PATCH http://localhost:8080/cmd/afiliaciones/{afiliacionId}/aprobar
Authorization: Bearer {token}
Content-Type: application/json

{
  "observaciones": "Aprobada después de revisión"
}
```

### 3. Rechazar Afiliación (Requiere ADMIN_GLOBAL)
```http
PATCH http://localhost:8080/cmd/afiliaciones/{afiliacionId}/rechazar
Authorization: Bearer {token}
Content-Type: application/json

{
  "observaciones": "No cumple requisitos"
}
```

---

## 🔍 Afiliaciones (Query - Lectura)

### 1. Buscar Afiliaciones por Solicitante
```http
GET http://localhost:8080/qry/afiliaciones?solicitante={usuarioId}
X-User-Id: {usuarioId}
X-User-Roles: ADMIN_GLOBAL
```

### 2. Buscar Afiliaciones por Zona
```http
GET http://localhost:8080/qry/afiliaciones?zonaId={zonaId}
X-User-Id: {usuarioId}
X-User-Roles: ADMIN_GLOBAL
```

### 3. Obtener Detalle de Afiliación
```http
GET http://localhost:8080/qry/afiliaciones/{afiliacionId}
X-User-Id: {usuarioId}
X-User-Roles: ADMIN_GLOBAL
```

---

## 🛍️ Productos

### 1. Listar Todos los Productos
```http
GET http://localhost:8080/productos/api/productos
```
**Nota:** Requiere pasar por API Gateway (header `X-Gateway-Passed: true` agregado automáticamente)

### 2. Consultar Producto por ID
```http
GET http://localhost:8080/productos/api/productos/{id}
```

---

## 📊 Health & Actuator Endpoints

### 1. Eureka Dashboard
```
http://localhost:8761
```

### 2. Health Check de Accounts Service
```
http://localhost:8081/actuator/health
```

### 3. Health Check de Productos Service
```
http://localhost:5001/actuator/health
```

### 4. RabbitMQ Management (Si está corriendo)
```
http://localhost:15672
Usuario: guest
Contraseña: guest
```

---

## 🧪 Ejemplo de Flujo Completo

### Paso 1: Obtener Token de Admin
```bash
curl -X POST http://localhost:8080/auth/dev/token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USR-ADMIN-GLOBAL",
    "roles": ["ADMIN_GLOBAL"]
  }'
```

### Paso 2: Registrar un Usuario
```bash
curl -X POST http://localhost:8080/cmd/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@ejemplo.com",
    "nombre": "Usuario Test"
  }'
```

### Paso 3: Solicitar Afiliación (usando token)
```bash
curl -X POST http://localhost:8080/cmd/afiliaciones/solicitar \
  -H "Authorization: Bearer {token_del_paso_1}" \
  -H "Content-Type: application/json" \
  -d '{
    "zonaId": "ZONA-001",
    "nombreVereda": "Vereda Test",
    "municipio": "Bogotá",
    "representanteNombre": "Test User",
    "representanteDocumento": "12345678",
    "representanteCorreo": "test@ejemplo.com"
  }'
```

### Paso 4: Consultar Afiliaciones
```bash
curl -X GET "http://localhost:8080/qry/afiliaciones?solicitante=USR-ADMIN-GLOBAL" \
  -H "X-User-Id: USR-ADMIN-GLOBAL" \
  -H "X-User-Roles: ADMIN_GLOBAL"
```

---

## 🔑 Notas Importantes

1. **Tokens JWT**: Los tokens generados con `/auth/dev/token` tienen un TTL de 3600 segundos (1 hora) por defecto.
2. **Headers Requeridos**:
   - `Authorization: Bearer {token}` para endpoints protegidos
   - `X-Gateway-Passed: true` se agrega automáticamente por el API Gateway
   - `X-User-Id` y `X-User-Roles` para queries con redacción de datos
3. **Usuario Admin Bootstrap**: Ya existe un usuario `USR-ADMIN-GLOBAL` creado por Flyway (migración V3).
4. **CORS**: El API Gateway está configurado para aceptar requests de `http://localhost:5173` y `http://localhost:3000`.

---

## 📝 Herramientas Recomendadas

- **Postman**: Para pruebas de API
- **cURL**: Para pruebas desde terminal
- **Thunder Client** (VS Code): Extensión para pruebas de API
- **Insomnia**: Cliente REST alternativo
