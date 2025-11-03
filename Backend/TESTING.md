# Guía de Testing - AgroMercado

## Descripción General

Esta guía documenta las pruebas implementadas para los microservicios de AgroMercado. Debido a la complejidad de la arquitectura (DDD, Event Sourcing, CQRS), nos enfocamos en las pruebas más críticas y viables.

## Estructura de Pruebas

### 🔵 Pedidos Service

Cobertura completa de pruebas para el servicio de pedidos:

#### **Pruebas Unitarias** (`PedidoServiceTest.java`)
- Obtención y gestión del carrito de compras
- Agregar/eliminar productos del carrito
- Confirmación de pedidos
- Procesamiento de pagos simulados
- Listado de pedidos por cliente y zona
- Actualización de estados

#### **Pruebas de Integración** (`CarritoControllerIntegrationTest.java`)
- Endpoints REST del carrito
- Validaciones de request/response
- Manejo de errores HTTP

#### **Pruebas E2E** (`PedidoE2ETest.java`)
- Flujo completo: Carrito → Agregar productos → Confirmar → Pagar
- Simulación de usuario cliente

---

### 🟢 Productos Service

✅ **Test básico implementado** (`ProductosApplicationTests.java`)

**Qué verifica:**
- ✅ El contexto de Spring se carga correctamente
- ✅ La base de datos H2 se configura en modo test
- ✅ Todas las dependencias y beans se inicializan sin errores
- ✅ Flyway está deshabilitado en modo test
- ✅ Eureka está deshabilitado en modo test

⚠️ **Nota sobre arquitectura**: Este servicio tiene una arquitectura legacy (`capaControladores`, `capaServicios`, etc.). Para tests más avanzados se recomienda refactorizar primero.

---

### 🟡 Accounts Service

✅ **Test básico implementado** (`AccountsServiceApplicationTests.java`)

**Qué verifica:**
- ✅ El contexto de Spring se carga correctamente
- ✅ La conexión a PostgreSQL funciona
- ✅ La conexión a MongoDB (Event Store) funciona
- ✅ La conexión a RabbitMQ (mensajería) funciona
- ✅ Eureka se registra correctamente
- ✅ Todas las dependencias DDD, Event Sourcing y CQRS se inicializan

⚠️ **Nota sobre arquitectura**: Este servicio implementa DDD puro, Event Sourcing y CQRS. Los tests unitarios y de integración completos requieren:
  - Mock de Event Store (MongoDB)
  - Mock de proyecciones de lectura
  - Sincronización CMD/QRY
  - Handlers de eventos configurados

---

## Cómo Ejecutar las Pruebas

### Prerrequisitos

```bash
# Java 17 o superior
java -version

# Maven (incluido en cada servicio con mvnw)
```

### Ejecutar Pruebas de Pedidos Service

```bash
# Navegar al directorio del servicio
cd Backend/pedidos-service

# Ejecutar todas las pruebas
./mvnw test

# Ejecutar solo pruebas unitarias
./mvnw test -Dtest=PedidoServiceTest

# Ejecutar solo pruebas de integración
./mvnw test -Dtest=CarritoControllerIntegrationTest

# Ejecutar solo pruebas E2E
./mvnw test -Dtest=PedidoE2ETest

# Ejecutar con logs detallados
./mvnw test -X
```

### Ejecutar Pruebas de Productos Service

```bash
# Navegar al directorio del servicio
cd Backend/productos-service

# Ejecutar el test básico
./mvnw test
```

### Ejecutar Pruebas de Accounts Service

```bash
# Navegar al directorio del servicio
cd Backend/accounts-service

# Ejecutar el test básico (requiere PostgreSQL, MongoDB y RabbitMQ corriendo)
./mvnw test
```

> **Nota**: El test de `accounts-service` requiere que las siguientes infraestructuras estén corriendo:
> - PostgreSQL en `localhost:5432` con base de datos `accounts_cmd`
> - MongoDB en `localhost:27017` con credenciales `root/password`
> - RabbitMQ en `localhost:5672` con credenciales por defecto

### Generar Reporte de Cobertura

```bash
cd Backend/pedidos-service

# Ejecutar pruebas con cobertura
./mvnw test jacoco:report

# El reporte se genera en: target/site/jacoco/index.html
```

---

## Configuración de Pruebas

### Base de Datos H2 para Testing

Los servicios están configurados para usar H2 (base de datos en memoria) durante las pruebas:

**`src/test/resources/application-test.yml`** (ejemplo):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  h2:
    console:
      enabled: true

# Deshabilitar Eureka en pruebas
eureka:
  client:
    enabled: false
```

### Perfiles de Testing

Las pruebas usan el perfil `test` mediante la anotación:

```java
@ActiveProfiles("test")
```

---

## Estructura de Archivos de Testing

```
Backend/
├── pedidos-service/
│   └── src/
│       └── test/
│           ├── java/
│           │   └── com/agromercado/pedidos/
│           │       ├── application/
│           │       │   └── service/
│           │       │       └── PedidoServiceTest.java
│           │       ├── api/
│           │       │   └── controller/
│           │       │       └── CarritoControllerIntegrationTest.java
│           │       └── e2e/
│           │           └── PedidoE2ETest.java
│           └── resources/
│               └── application-test.yml
│
├── productos-service/
│   └── src/test/resources/
│       └── application-test.yml
│
└── accounts-service/
    └── src/test/resources/
        └── application-test.yml
```

---

## Solución de Problemas

### Error: "Cannot find main class"

```bash
# Recompilar el servicio
./mvnw clean compile
```

### Error: "Port already in use"

Las pruebas E2E levantan el servidor en un puerto aleatorio automáticamente. Si aun así tienes conflictos:

```bash
# Verificar puertos en uso
netstat -ano | findstr :8080
netstat -ano | findstr :5001
netstat -ano | findstr :5002
netstat -ano | findstr :5003

# Matar proceso si es necesario (reemplaza <PID> con el ID del proceso)
taskkill /PID <PID> /F
```

### Error: "Eureka connection refused" durante las pruebas

Asegúrate de que el perfil `test` está activo y que Eureka está deshabilitado en `application-test.yml`:

```yaml
eureka:
  client:
    enabled: false
```

### Tests fallan por timeout

Aumenta el timeout en las pruebas:

```java
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS) // 30 segundos de timeout
void miPrueba() {
    // ...
}
```

---

## Mejores Prácticas

1. **Aislar las pruebas**: Usar `@Transactional` en pruebas de integración para hacer rollback automático.

2. **Mock de servicios externos**: Mockear llamadas al `ProductoClientService` y otros servicios externos.

3. **Datos de prueba consistentes**: Usar `@BeforeEach` para inicializar datos limpios en cada test.

4. **Nombres descriptivos**: Usar `@DisplayName` para documentar qué prueba cada test.

5. **Assertions claras**: Usar AssertJ (`assertThat`) para mensajes de error más descriptivos.

---

## Comandos Útiles

```bash
# Compilar sin ejecutar pruebas
./mvnw clean package -DskipTests

# Ejecutar solo una prueba específica
./mvnw test -Dtest=PedidoServiceTest#debeCrearCarritoVacio

# Ejecutar pruebas en modo debug
./mvnw test -Dmaven.surefire.debug

# Limpiar y ejecutar pruebas
./mvnw clean test

# Ver logs detallados
./mvnw test -X
```

---

## Notas de Arquitectura

### ¿Por qué no hay tests para Productos y Accounts?

1. **Productos Service**: Arquitectura legacy que necesita refactorización
2. **Accounts Service**: DDD + Event Sourcing requiere infraestructura compleja de testing

### Recomendaciones futuras

1. **Productos**: Migrar a arquitectura de capas estándar y agregar tests
2. **Accounts**: Implementar tests de contrato (Contract Testing) para eventos de dominio
3. **Todos**: Agregar Testcontainers para pruebas con PostgreSQL real

---

## Contacto y Soporte

Para preguntas sobre las pruebas o para reportar problemas, contacta al equipo de desarrollo.

---

**Última actualización**: Noviembre 2025
