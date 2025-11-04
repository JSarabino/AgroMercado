# Guía de Configuración - Pedidos Service

## Paso 1: Crear la Base de Datos

Abre PostgreSQL y ejecuta:

```sql
CREATE DATABASE pedidos_db;
```

O ejecuta el script de inicialización:

```bash
psql -U postgres -f Backend/pedidos-service/init-pedidos-db.sql
```

## Paso 2: Verificar Servicios Requeridos

Asegúrate de que estos servicios estén corriendo:

1. **Eureka Server** (puerto 8761)
2. **API Gateway** (puerto 8080)
3. **productos-service** (puerto 5001)
4. **PostgreSQL** (puerto 5432)

## Paso 3: Iniciar el Servicio

### Desde VS Code:

1. Abre el panel "Run and Debug" (`Ctrl+Shift+D`)
2. Selecciona "Spring Boot-PedidosApplication<pedidos-service>"
3. Click en el botón verde ▶️

### Desde Terminal:

```bash
cd Backend/pedidos-service
../../mvnw.cmd spring-boot:run  # Windows
# o
./mvnw spring-boot:run  # Linux/Mac
```

## Paso 4: Verificar que el Servicio Está Corriendo

### Verificar Health Check:
```bash
curl http://localhost:5002/actuator/health
```

### Verificar Registro en Eureka:
Abre http://localhost:8761 y verifica que "PEDIDOS-SERVICE" aparezca en la lista.

## Paso 5: Probar los Endpoints

### A través del API Gateway:

Todos los endpoints deben llamarse a través del API Gateway en `http://localhost:8080`

#### 1. Obtener el Carrito (o crear uno nuevo)

```bash
curl -X GET http://localhost:8080/carrito \
  -H "X-User-Id: USR-ADMIN-GLOBAL" \
  -H "X-User-Name: Admin Global" \
  -H "X-User-Email: root@agromercado.local"
```

#### 2. Agregar un Producto al Carrito

Primero asegúrate de tener productos disponibles en el catálogo.

```bash
curl -X POST http://localhost:8080/carrito/agregar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: USR-ADMIN-GLOBAL" \
  -H "X-User-Name: Admin Global" \
  -H "X-User-Email: root@agromercado.local" \
  -d '{
    "productoId": 1,
    "cantidad": 3
  }'
```

#### 3. Confirmar el Pedido

```bash
curl -X POST http://localhost:8080/pedidos/confirmar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: USR-ADMIN-GLOBAL" \
  -d '{
    "direccionEntrega": "Calle 123 #45-67, Popayán",
    "telefonoContacto": "3001234567",
    "metodoPago": "TARJETA_CREDITO",
    "notas": "Llamar antes de entregar"
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "clienteId": "USR-ADMIN-GLOBAL",
  "numeroPedido": "PED-1730654321000",
  "estado": "PENDIENTE",
  "estadoPago": "PENDIENTE",
  "total": 125000.50,
  ...
}
```

#### 4. Procesar el Pago

```bash
curl -X POST http://localhost:8080/pagos/procesar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: USR-ADMIN-GLOBAL" \
  -d '{
    "pedidoId": 1,
    "metodoPago": "TARJETA_CREDITO",
    "numeroTarjeta": "4111111111111111",
    "nombreTitular": "Admin Global",
    "fechaVencimiento": "12/25",
    "cvv": "123"
  }'
```

**Respuesta esperada (pago aprobado):**
```json
{
  "transaccionId": "TDC-A1B2C3D4",
  "aprobado": true,
  "mensaje": "Pago procesado exitosamente",
  "monto": 125000.50,
  "metodoPago": "TARJETA_CREDITO",
  "pedidoId": 1,
  "numeroPedido": "PED-1730654321000"
}
```

#### 5. Ver Mis Pedidos

```bash
curl -X GET http://localhost:8080/pedidos/mis-pedidos \
  -H "X-User-Id: USR-ADMIN-GLOBAL"
```

## Solución de Problemas

### Error: "No se pudo obtener la información del producto"

**Causa**: El servicio de productos no está corriendo o no hay productos disponibles.

**Solución**:
1. Verifica que `productos-service` esté corriendo en puerto 5001
2. Crea algunos productos usando el endpoint `/productos`

### Error: "Carrito está vacío"

**Causa**: Intentaste confirmar un pedido sin productos en el carrito.

**Solución**: Agrega productos al carrito antes de confirmar.

### Error: "Pedido no está en estado pendiente"

**Causa**: Intentaste pagar un pedido que ya fue pagado o está en otro estado.

**Solución**: Solo se pueden pagar pedidos en estado `PENDIENTE`.

### El servicio no se registra en Eureka

**Causa**: Eureka Server no está corriendo.

**Solución**:
1. Inicia Eureka Server primero
2. Espera 30 segundos
3. Reinicia pedidos-service

### Error de conexión a la base de datos

**Causa**: La base de datos `pedidos_db` no existe o PostgreSQL no está corriendo.

**Solución**:
1. Verifica que PostgreSQL esté corriendo
2. Crea la base de datos: `CREATE DATABASE pedidos_db;`
3. Verifica las credenciales en `application.yml`

## Integración con el Frontend

### Headers Requeridos

Todos los endpoints requieren estos headers:
- `X-User-Id`: ID del usuario (obligatorio)
- `X-User-Name`: Nombre del usuario (obligatorio)
- `X-User-Email`: Email del usuario (opcional)

Estos headers normalmente los proporciona el API Gateway después de validar el JWT.

### Ejemplo de Integración en React

```typescript
// services/pedidos.service.ts
import { apiService } from './api.service';

export const pedidosService = {
  // Obtener carrito
  obtenerCarrito: () => {
    return apiService.get('/carrito');
  },

  // Agregar producto al carrito
  agregarProducto: (productoId: number, cantidad: number) => {
    return apiService.post('/carrito/agregar', { productoId, cantidad });
  },

  // Confirmar pedido
  confirmarPedido: (datos) => {
    return apiService.post('/pedidos/confirmar', datos);
  },

  // Procesar pago
  procesarPago: (datos) => {
    return apiService.post('/pagos/procesar', datos);
  },

  // Listar mis pedidos
  listarMisPedidos: () => {
    return apiService.get('/pedidos/mis-pedidos');
  }
};
```

## Flujo Completo de Compra

```
1. Cliente → [GET /carrito] → Obtener carrito vacío o existente
2. Cliente → [POST /carrito/agregar] → Agregar productos (repetir)
3. Cliente → [GET /carrito] → Revisar carrito
4. Cliente → [POST /pedidos/confirmar] → Confirmar pedido
   → Estado cambia a PENDIENTE
5. Cliente → [POST /pagos/procesar] → Procesar pago
   → Si aprobado: Estado cambia a PAGADO/EN_PREPARACION
   → Si rechazado: Estado de pago cambia a RECHAZADO
6. Admin → [PATCH /pedidos/{id}/estado] → Actualizar a ENVIADO
7. Admin → [PATCH /pedidos/{id}/estado] → Actualizar a ENTREGADO
```

## Notas de Seguridad

⚠️ **IMPORTANTE**: La configuración actual está en modo desarrollo y permite acceso sin autenticación. Antes de producción:

1. Habilitar autenticación JWT en el API Gateway
2. Validar que los usuarios solo puedan acceder a sus propios pedidos
3. Implementar roles (CLIENTE, ADMIN_ZONA, etc.)
4. Integrar con pasarela de pago real
5. Implementar HTTPS

## Monitoreo

### Logs
Los logs se pueden ver en la consola o en el archivo de logs de Spring Boot.

### Actuator Endpoints
- Health: http://localhost:5002/actuator/health
- Info: http://localhost:5002/actuator/info

## Base de Datos

### Consultas Útiles

```sql
-- Ver todos los pedidos
SELECT * FROM pedidos ORDER BY fecha_creacion DESC;

-- Ver detalles de un pedido
SELECT p.*, d.*
FROM pedidos p
LEFT JOIN detalles_pedido d ON p.id = d.pedido_id
WHERE p.id = 1;

-- Ver carritos activos
SELECT * FROM pedidos WHERE estado = 'CARRITO';

-- Ver pedidos pendientes de pago
SELECT * FROM pedidos WHERE estado = 'PENDIENTE' AND estado_pago = 'PENDIENTE';

-- Limpiar carritos antiguos (más de 7 días sin actividad)
DELETE FROM pedidos
WHERE estado = 'CARRITO'
AND fecha_creacion < NOW() - INTERVAL '7 days';
```

## Próximos Pasos

1. Crear componentes en el Frontend para:
   - Catálogo de productos con botón "Agregar al carrito"
   - Vista del carrito de compras
   - Checkout (confirmación de pedido)
   - Página de pago
   - Historial de pedidos
   - Seguimiento de pedidos

2. Implementar notificaciones:
   - Email al confirmar pedido
   - SMS/Push al cambiar estado del pedido

3. Integrar con pasarela de pago real

4. Implementar sistema de descuentos y cupones
