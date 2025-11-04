# Pedidos Service - Microservicio de Gestión de Pedidos

Microservicio para gestionar el carrito de compras, pedidos y procesamiento de pagos simulados.

## Características

- ✅ **Carrito de Compras**: Gestión completa del carrito del cliente
- ✅ **Gestión de Pedidos**: Confirmar y rastrear pedidos
- ✅ **Pago Simulado**: Procesamiento de pagos con múltiples métodos
- ✅ **Integración con Productos**: Obtiene información actualizada de productos
- ✅ **Multi-tenancy**: Soporte para múltiples zonas
- ✅ **Registro en Eureka**: Service discovery automático

## Tecnologías

- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (migraciones de base de datos)
- **Spring Cloud Netflix Eureka** (service discovery)
- **Lombok**

## Puertos

- **Servicio**: 5002
- **PostgreSQL**: 5432 (base de datos: `pedidos_db`)

## Configuración

### Base de Datos

Ejecutar el script de inicialización:

```bash
psql -U postgres -f Backend/pedidos-service/init-pedidos-db.sql
```

### Variables de Entorno

```properties
POSTGRES_USER=postgres
POSTGRES_PASSWORD=root
```

## Iniciar el Servicio

### Opción 1: Desde VS Code

1. Ir a "Run and Debug" (Ctrl+Shift+D)
2. Seleccionar "Spring Boot-PedidosApplication<pedidos-service>"
3. Click en el botón de play

### Opción 2: Desde línea de comandos

```bash
cd Backend/pedidos-service
./mvnw spring-boot:run
```

## Endpoints API

### Carrito de Compras

#### Obtener Carrito
```http
GET /carrito
Headers:
  X-User-Id: {clienteId}
  X-User-Name: {nombre}
  X-User-Email: {email}
```

#### Agregar Producto al Carrito
```http
POST /carrito/agregar
Headers:
  X-User-Id: {clienteId}
  X-User-Name: {nombre}
Body:
{
  "productoId": 1,
  "cantidad": 2
}
```

#### Eliminar Producto del Carrito
```http
DELETE /carrito/items/{detalleId}
Headers:
  X-User-Id: {clienteId}
```

#### Vaciar Carrito
```http
DELETE /carrito
Headers:
  X-User-Id: {clienteId}
```

### Pedidos

#### Confirmar Pedido
```http
POST /pedidos/confirmar
Headers:
  X-User-Id: {clienteId}
Body:
{
  "direccionEntrega": "Calle 123 #45-67",
  "telefonoContacto": "3001234567",
  "metodoPago": "TARJETA_CREDITO",
  "notas": "Entregar en la mañana"
}
```

#### Obtener Pedido
```http
GET /pedidos/{id}
```

#### Listar Mis Pedidos
```http
GET /pedidos/mis-pedidos
Headers:
  X-User-Id: {clienteId}
```

#### Actualizar Estado del Pedido
```http
PATCH /pedidos/{id}/estado?estado=EN_PREPARACION
```

### Pagos

#### Procesar Pago
```http
POST /pagos/procesar
Headers:
  X-User-Id: {clienteId}
Body:
{
  "pedidoId": 123,
  "metodoPago": "TARJETA_CREDITO",
  "numeroTarjeta": "4111111111111111",
  "nombreTitular": "Juan Perez",
  "fechaVencimiento": "12/25",
  "cvv": "123"
}
```

**Nota**: El pago es simulado y tiene 90% de probabilidad de ser aprobado.

## Modelos de Datos

### Estados de Pedido
- `CARRITO`: En el carrito de compras (no confirmado)
- `PENDIENTE`: Pedido confirmado, esperando pago
- `PAGADO`: Pago confirmado
- `EN_PREPARACION`: En preparación
- `ENVIADO`: Enviado al cliente
- `ENTREGADO`: Entregado
- `CANCELADO`: Cancelado

### Estados de Pago
- `PENDIENTE`: Esperando pago
- `PROCESANDO`: Procesando pago
- `APROBADO`: Pago aprobado
- `RECHAZADO`: Pago rechazado
- `REEMBOLSADO`: Pago reembolsado

### Métodos de Pago
- `TARJETA_CREDITO`
- `TARJETA_DEBITO`
- `TRANSFERENCIA`
- `EFECTIVO`
- `PSE`

## Flujo de Compra

1. **Cliente agrega productos al carrito**
   ```
   POST /carrito/agregar
   ```

2. **Cliente revisa su carrito**
   ```
   GET /carrito
   ```

3. **Cliente confirma el pedido**
   ```
   POST /pedidos/confirmar
   ```

4. **Cliente procesa el pago**
   ```
   POST /pagos/procesar
   ```

5. **El pedido cambia a estado PAGADO/EN_PREPARACION**

## Integración con Otros Servicios

### productos-service
- Obtiene información de productos al agregar al carrito
- Valida disponibilidad de productos
- URL configurada en: `productos.service.url`

### accounts-service
- Recibe información del cliente en headers (X-User-Id, X-User-Name)

## Base de Datos

### Tablas

#### pedidos
Almacena información de pedidos y carritos

#### detalles_pedido
Almacena los productos en cada pedido con cantidades y precios

### Migraciones
Las migraciones se ejecutan automáticamente con Flyway al iniciar el servicio.

Ubicación: `src/main/resources/db/migration/`

## Simulación de Pagos

El servicio incluye un simulador de pagos para desarrollo:

- **Tiempo de procesamiento**: 2 segundos
- **Tasa de aprobación**: 90%
- **Genera transacciones únicas** con prefijos según método de pago

### Ejemplo de Respuesta de Pago Exitoso
```json
{
  "transaccionId": "TDC-A1B2C3D4",
  "aprobado": true,
  "mensaje": "Pago procesado exitosamente",
  "monto": 125000.50,
  "metodoPago": "TARJETA_CREDITO",
  "pedidoId": 123,
  "numeroPedido": "PED-1730654321000"
}
```

## Logs

El servicio genera logs detallados de:
- Creación de carritos
- Agregado de productos
- Confirmación de pedidos
- Procesamiento de pagos
- Cambios de estado

Nivel de logs: `DEBUG` para `com.agromercado.pedidos`

## Actuator

Endpoints de monitoreo disponibles:

- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio

## Desarrollo

### Compilar
```bash
./mvnw clean package
```

### Tests
```bash
./mvnw test
```

## Notas Importantes

⚠️ **Pago Simulado**: Este servicio usa un simulador de pagos solo para desarrollo. En producción debe integrarse con una pasarela de pago real (Stripe, PayPal, etc.)

⚠️ **Seguridad**: La configuración actual permite acceso sin autenticación para desarrollo. En producción debe implementarse JWT/OAuth2.

⚠️ **Multi-tenancy**: El campo `zonaId` permite segmentar pedidos por zona geográfica.

## Próximas Mejoras

- [ ] Integración con pasarela de pago real
- [ ] Notificaciones por email al confirmar pedido
- [ ] Notificaciones push de cambios de estado
- [ ] Sistema de descuentos y cupones
- [ ] Histórico de precios de productos
- [ ] Reportes de ventas
- [ ] Integración con sistema de inventario
