# 📋 Arquitectura de Solicitudes de Afiliación

Este documento explica la separación de las dos tablas de solicitudes en el sistema AgroMercado.

## 🏗️ Dos Flujos Independientes

### 1️⃣ **Afiliaciones de Zona** (Tabla: `afiliaciones_zona`)

**Flujo:** Líder Comunitario → Admin Global

- **Propósito:** Un líder comunitario solicita crear una nueva zona en el sistema
- **Tabla PostgreSQL:** `afiliaciones_zona`
- **Vista MongoDB:** `afiliaciones_zona_view`
- **Estado:** PENDIENTE → APROBADA/RECHAZADA (por ADMIN_GLOBAL)

**Endpoints:**
- `POST /cmd/afiliaciones/solicitar` - Solicitar nueva zona
- `PATCH /cmd/afiliaciones/{id}/aprobar` - Aprobar zona (ADMIN_GLOBAL)
- `PATCH /cmd/afiliaciones/{id}/rechazar` - Rechazar zona (ADMIN_GLOBAL)
- `GET /qry/afiliaciones` - Consultar zonas

**Características:**
- Orientado a eventos (Event Sourcing con tabla `outbox`)
- Patrón CQRS completo
- Al aprobar, se crea la zona y se asigna el rol ADMIN_ZONA al solicitante

---

### 2️⃣ **Solicitudes de Productor** (Tabla: `solicitudes_afiliacion_productor`)

**Flujo:** Productor → Admin de Zona

- **Propósito:** Un productor solicita unirse a una zona YA EXISTENTE
- **Tabla PostgreSQL:** `solicitudes_afiliacion_productor`
- **Vista MongoDB:** `solicitudes_productor_view`
- **Estado:** PENDIENTE → APROBADA/RECHAZADA (por ADMIN_ZONA)

**Endpoints:**
- `POST /cmd/solicitudes-productor/solicitar` - Productor solicita afiliación
- `POST /cmd/solicitudes-productor/{id}/aprobar` - Aprobar (ADMIN_ZONA)
- `POST /cmd/solicitudes-productor/{id}/rechazar` - Rechazar (ADMIN_ZONA)
- `GET /qry/solicitudes-productor` - Consultar solicitudes

**Características:**
- Base de datos independiente para mejor escalabilidad
- Patrón CQRS con sincronización por polling (cada 5 segundos)
- Al aprobar, se otorga membresía al productor en la zona
- Evita solicitudes duplicadas (un productor no puede tener múltiples solicitudes pendientes a la misma zona)

---

## 🔄 Sincronización CQRS

### Afiliaciones de Zona
```
PostgreSQL (afiliaciones_zona)
    ↓ [Event Sourcing + Outbox Pattern]
MongoDB (afiliaciones_zona_view)
```

### Solicitudes de Productor
```
PostgreSQL (solicitudes_afiliacion_productor)
    ↓ [Polling cada 5 segundos - SolicitudProductorProjector]
MongoDB (solicitudes_productor_view)
```

---

## 📊 Diagrama de Flujo

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUJO 1: ZONAS                           │
│  Líder Comunitario → ADMIN_GLOBAL → Zona Creada            │
│  (afiliaciones_zona)                                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│               FLUJO 2: PRODUCTORES                          │
│  Productor → ADMIN_ZONA → Membresía Otorgada               │
│  (solicitudes_afiliacion_productor)                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Estructura de Datos

### solicitudes_afiliacion_productor (PostgreSQL)
```sql
- solicitud_id (PK)
- zona_id
- productor_usuario_id
- nombre_productor
- documento
- telefono
- correo
- direccion
- tipo_productos
- estado (PENDIENTE/APROBADA/RECHAZADA)
- observaciones
- aprobada_por
- fecha_decision
- version
- created_at
- updated_at
```

### solicitudes_productor_view (MongoDB)
```javascript
{
  _id: "SOLPROD-...",
  zonaId: "ZONA-001",
  productorUsuarioId: "USR-...",
  nombreProductor: "Juan Pérez",
  documento: "12345678",
  telefono: "3001234567",
  correo: "juan@ejemplo.com",
  direccion: "Finca La Esperanza",
  tipoProductos: "Café, Plátano",
  estado: "PENDIENTE",
  observaciones: null,
  aprobadaPor: null,
  fechaDecision: null,
  version: 1,
  createdAt: "2025-11-02T...",
  updatedAt: "2025-11-02T..."
}
```

---

## 🔒 Permisos

| Endpoint | Productor | ADMIN_ZONA | ADMIN_GLOBAL |
|----------|-----------|------------|--------------|
| Solicitar afiliación a zona | ✅ | ✅ | ✅ |
| Aprobar/Rechazar solicitud | ❌ | ✅ (su zona) | ✅ (todas) |
| Ver solicitudes propias | ✅ | ❌ | ❌ |
| Ver solicitudes de zona | ❌ | ✅ (su zona) | ✅ (todas) |

---

## 🚀 Ejemplo de Uso

### Paso 1: Productor solicita afiliación
```bash
curl -X POST http://localhost:8080/cmd/solicitudes-productor/solicitar \
  -H "Authorization: Bearer {token_productor}" \
  -H "Content-Type: application/json" \
  -d '{
    "zonaId": "ZONA-001",
    "nombreProductor": "Juan Pérez",
    "documento": "12345678",
    "telefono": "3001234567",
    "correo": "juan@ejemplo.com",
    "direccion": "Finca La Esperanza",
    "tipoProductos": "Café, Plátano, Yuca"
  }'
```

### Paso 2: Admin de zona consulta solicitudes pendientes
```bash
curl -X GET "http://localhost:8080/qry/solicitudes-productor/zona/ZONA-001/pendientes" \
  -H "Authorization: Bearer {token_admin_zona}"
```

### Paso 3: Admin de zona aprueba la solicitud
```bash
curl -X POST http://localhost:8080/cmd/solicitudes-productor/SOLPROD-xyz789/aprobar \
  -H "Authorization: Bearer {token_admin_zona}" \
  -H "Content-Type: application/json" \
  -d '{
    "observaciones": "Bienvenido a la zona"
  }'
```

---

## ⚠️ Validaciones

### Solicitud de Productor
- ✅ No puede haber solicitudes duplicadas pendientes (mismo productor + misma zona)
- ✅ Solo se puede aprobar/rechazar solicitudes en estado PENDIENTE
- ✅ El ID del productor viene del JWT (no se puede falsificar)

### Sincronización
- ✅ El projector sincroniza cada 5 segundos
- ✅ Se compara la versión para evitar sobrescrituras innecesarias
- ✅ En producción, se debe usar CDC (Change Data Capture) o eventos

---

## 🔧 Mejoras Futuras

1. **Event Sourcing para Solicitudes de Productor**: Actualmente usa polling, se puede migrar a eventos
2. **Notificaciones**: Enviar email/SMS al aprobar/rechazar
3. **Workflow más complejo**: Estados intermedios (EN_REVISION, DOCUMENTACION_PENDIENTE, etc.)
4. **Validación de Zona**: Verificar que el admin que aprueba sea efectivamente admin de esa zona
5. **Historial de cambios**: Tabla de auditoría para ver quién modificó qué y cuándo
