# ☕ Savana Data Coffee — Guía Maestra de Integración Frontend-Backend (`instructions.md`)

Este documento técnico contiene toda la especificación necesaria para que el equipo de **Front-end** integre la interfaz de usuario con el **API Backend V2 de Savana Data Coffee**, incluyendo el manejo de sesiones JWT, el flujo de doble factor (2FA), la matriz de permisos por rol, las reglas críticas de negocio, los contratos JSON de cada módulo y un recorrido general paso a paso para probar y consumir toda la API.

---

## 1. Base URL y Configuración Global

### 1.1. Servidor y CORS
- **Base URL (Entorno Local):** `http://localhost:8080`
- **Prefijo de API:** `/api`
- **Orígenes CORS habilitados por defecto:** `http://localhost:4200` (Angular) y `http://localhost:3000` (React / Next.js / Vite).
- **Content-Type obligatorio:** `application/json`

### 1.2. Envío del Token JWT en Cabeceras HTTP
Todas las rutas protegidas (operaciones de **Administrador** y **Empleado**) requieren que el Front-end adjunte el token JWT en el header `Authorization` con el esquema `Bearer`:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi...
Content-Type: application/json
Accept: application/json
```

> **Recomendación para el Front-end (Interceptor HTTP):**
> Configura un `HttpInterceptor` (Angular) o interceptor de `Axios`/`Fetch` que lea el token almacenado en `localStorage` / `sessionStorage` y lo inyecte automáticamente en todas las peticiones, excepto en las rutas públicas (`/api/auth/login`, `/api/auth/verificar-2fa`, consulta de menú y reservas de clientes).

---

## 2. Flujo de Autenticación, Cuentas Semilla y Seguridad (¡El Más Importante!)

### 2.1. Clientes (Público — Sin Cuenta ni Contraseña)
- **Regla de Negocio:** Los clientes (comensales) **NO tienen cuenta de usuario, NO tienen contraseña y NO usan JWT**.
- El cliente interactúa libremente con el menú público y con el módulo de reservas ingresando únicamente sus datos básicos (`nombre`, `apellido`, `correo`, `telefono`).
- Su **`correo` electrónico es único** y actúa como su identificador natural:
  - Si un cliente reserva por primera vez, el backend lo registra y lo **guarda permanentemente** en la tabla `cliente`.
  - Si el correo ya existe en la base de datos, el backend reutiliza automáticamente su `idCliente`.
  - El cliente puede consultar todas sus reservas históricas o activas ingresando su correo en `GET /api/reservas/correo/{correo}` o `GET /api/reservas?correo={correo}`.

### 2.2. Cuentas Semilla Precargadas para Pruebas (Staff)
Al levantar el backend, la base de datos ya cuenta con los siguientes usuarios de personal (`Staff`) listos para usar:

| Rol en Sistema | Correo (`correo`) | Contraseña (`contrasena`) | 2FA (`dosFacHab`) | Uso Principal |
| :--- | :--- | :--- | :--- | :--- |
| **`ADMINISTRADOR`** | `admin@savanacoffee.com` | `Password123` | `false` (Login directo) | Gestión de meseros, catálogo, mesas, clientes y dashboard de KPIs. |
| **`MESERO`** | `mesero@savanacoffee.com` | `Password123` | `false` (Login directo) | Toma de pedidos en salón, cambio de estados de comanda y cobro/facturación. |
| **`MESERO`** | `luisa@savanacoffee.com` | `Password123` | `false` (Login directo) | Cuenta secundaria operativa de salón. |

### 2.3. Flujo de Login del Staff (`ADMINISTRADOR` y `EMPLEADO`) y Doble Factor (2FA)

El proceso de inicio de sesión para el personal (`POST /api/auth/login`) soporta tanto acceso directo como verificación en dos pasos (2FA) mediante código OTP de 6 dígitos enviado por correo HTML (almacenado en memoria del servidor con vigencia de **5 minutos**).

```text
[Pantalla de Login]
   │  POST /api/auth/login { correo, contrasena }
   ▼
¿Respuesta tiene requiere2FA == true?
   ├── NO  ──► Guardar `res.token` y `res.usuario` ──► Redirigir según `res.usuario.rol`
   │
   └── SÍ  ──► Mostrar [Pantalla Código OTP 6 dígitos]
                  │  POST /api/auth/verificar-2fa { correo, codigoOtp }
                  ▼
               Guardar `res.token` y `res.usuario` ──► Redirigir según `res.usuario.rol`
```

#### Caso A: Login Directo (`dosFacHab = false`)
**Request (`POST /api/auth/login`):**
```json
{
  "correo": "admin@savanacoffee.com",
  "contrasena": "Password123"
}
```
**Response (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipoToken": "Bearer",
  "requiere2FA": false,
  "mensaje": "Inicio de sesión exitoso.",
  "usuario": {
    "idUsuario": 1,
    "nombres": "Administrador",
    "apellidos": "General",
    "nombre": "Administrador General",
    "correo": "admin@savanacoffee.com",
    "rol": "ADMINISTRADOR",
    "estado": "ACTIVO",
    "dosFacHab": false,
    "fechaInicio": "2026-10-18"
  }
}
```

#### Caso B: Login con 2FA Habilitado (`dosFacHab = true`)
1. **Paso 1 (`POST /api/auth/login`):** Devuelve `200 OK` pero con `"token": null` y `"requiere2FA": true`:
   ```json
   {
     "token": null,
     "tipoToken": null,
     "requiere2FA": true,
     "mensaje": "Se ha enviado un código de verificación (OTP) a su correo empleado2fa@savanacoffee.com. Válido por 5 minutos.",
     "usuario": {
       "idUsuario": 4,
       "nombres": "Camila",
       "apellidos": "Vargas",
       "correo": "empleado2fa@savanacoffee.com",
       "rol": "EMPLEADO",
       "estado": "ACTIVO",
       "dosFacHab": true
     }
   }
   ```
2. **Paso 2 (`POST /api/auth/verificar-2fa`):** El Front-end muestra un input para el código de 6 dígitos recibido por correo y envía:
   ```json
   {
     "correo": "empleado2fa@savanacoffee.com",
     "codigoOtp": "482910"
   }
   ```
   **Response (`200 OK`):** Entrega finalmente el `"token"` JWT listo para usar.

> **Nota sobre Empleados Inactivos:** Si un empleado tiene `"estado": "INACTIVO"`, el backend rechazará su login con `401 Unauthorized` indicando que su cuenta fue desactivada por administración.

---

## 3. Roles y Permisos (¿Qué debe mostrar y ocultar el Front-end?)

### 3.1. Vistas Públicas (Cliente — Sin Token JWT)
- **Catálogo / Menú Digital:** Consultar categorías (`GET /api/categorias?estado=ACTIVA`) y productos (`GET /api/productos?disponibilidad=true`).
- **Buscador de Disponibilidad de Mesas:** Consultar qué mesas están libres para una fecha, hora y número de personas (`GET /api/reservas/disponibles?fechaHora=2026-10-25T19:30:00&numeroPersonas=4`).
- **Formulario de Reserva:** Crear reserva enviando datos del cliente (`POST /api/reservas`).
- **Mis Reservas (Búsqueda por Correo):** Consultar todas las reservas asociadas a su correo electrónico (`GET /api/reservas/correo/{correo}`).

### 3.2. Vistas de Administrador (`rol === "ADMINISTRADOR"`)
- **Dashboard Operativo y Financiero:** Tarjetas de KPIs en tiempo real (`GET /api/dashboard/resumen`).
- **Gestión de Personal (`Staff` / Empleados):** Listar, registrar nuevos empleados/administradores, editar sus datos, activar/inactivar cuentas (`PATCH /api/usuarios/{id}/estado?estado=INACTIVO`) o eliminarlos (`DELETE /api/usuarios/{id}`).
- **Gestión del Menú:** Crear, editar y eliminar Categorías (`/api/categorias`) y Productos (`/api/productos`).
- **Gestión de Mesas y Directorio de Clientes:** Crear/eliminar mesas (`/api/mesas`) y editar/eliminar explícitamente clientes (`DELETE /api/clientes/{id}`).
- **Auditoría (Solo Lectura):** Ver listados e historial de Reservas, Pedidos, Facturas y Pagos.
- 🚫 **RESTRICCIÓN CRÍTICA EN UI PARA ADMINISTRADOR:**
  El Administrador **NUNCA toma pedidos ni procesa pagos**. El Front-end **DEBE OCULTAR O DESHABILITAR** los botones de *"Nuevo Pedido"*, *"Cambiar Estado de Pedido"* y *"Cobrar / Registrar Pago"* cuando el usuario logueado sea `ADMINISTRADOR` (el backend devolverá `403 Forbidden` si intenta llamar a `POST /api/pedidos`, `PATCH /api/pedidos/{id}/estado` o `POST /api/pagos`).

### 3.3. Vistas de Empleado / Mesero (`rol === "EMPLEADO"`)
- **Mapa de Mesas del Salón:** Ver todas las mesas (`GET /api/mesas`) y cambiar su estado operativo (`PATCH /api/mesas/{numeroMesa}/estado?nuevoEstado=OCUPADA`).
- **Comandas / Pedidos en Salón:** Crear pedidos en mesa (`POST /api/pedidos`), listar pedidos activos y avanzar su estado (`PATCH /api/pedidos/{id}/estado`).
- **Caja / Cobro y Facturación:** Procesar el pago de un pedido (`POST /api/pagos`), consultar facturas (`GET /api/facturas`) y métodos de pago (`GET /api/metodos-pago`).
- **Recepción de Clientes y Reservas:** Consultar reservas del día (`GET /api/reservas`), confirmar/completar/cancelar reservas (`PATCH /api/reservas/{id}/estado`) y registrar clientes presenciales en salón (`POST /api/clientes`).
- 🚫 **RESTRICCIÓN CRÍTICA EN UI PARA EMPLEADO:**
  El Empleado **NO puede** administrar empleados, **NO puede** crear/editar/eliminar productos ni categorías del menú, **NO puede** eliminar clientes ni mesas, y **NO puede** ver el Dashboard gerencial (`/api/dashboard/resumen`). Oculta esas secciones en el menú lateral para el rol `EMPLEADO`.

### 3.4. Tabla Resumen de Permisos por Endpoint

| Endpoint | Método HTTP | Acceso Permitido |
| :--- | :--- | :--- |
| `/api/auth/login`, `/api/auth/verificar-2fa` | `POST` | Público (`Sin Token`) |
| `/api/productos/**`, `/api/categorias/**` | `GET` | Público (`Sin Token`) |
| `/api/reservas/disponibles`, `/api/reservas/correo/**`, `/api/reservas?correo=...` | `GET` | Público (`Sin Token`) |
| `/api/reservas` | `POST` | Público (`Sin Token`) |
| `/api/pedidos`, `/api/pedidos/{id}/estado` | `POST`, `PATCH` | **Exclusivo `EMPLEADO`** (`403` para Admin) |
| `/api/pagos` | `POST` | **Exclusivo `EMPLEADO`** (`403` para Admin) |
| `/api/usuarios/**`, `/api/empleados/**`, `/api/staff/**` | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | **Exclusivo `ADMINISTRADOR`** |
| `/api/productos/**`, `/api/categorias/**` | `POST`, `PUT`, `DELETE` | **Exclusivo `ADMINISTRADOR`** |
| `/api/mesas`, `/api/mesas/{numeroMesa}` | `POST`, `DELETE` | **Exclusivo `ADMINISTRADOR`** |
| `/api/clientes/{id}` | `PUT`, `DELETE` | **Exclusivo `ADMINISTRADOR`** |
| `/api/dashboard/resumen` | `GET` | **Exclusivo `ADMINISTRADOR`** |
| `/api/mesas/**` | `GET`, `PUT`, `PATCH` | `ADMINISTRADOR` y `EMPLEADO` |
| `/api/pedidos/**`, `/api/facturas/**`, `/api/pagos/**`, `/api/metodos-pago/**` | `GET` | `ADMINISTRADOR` y `EMPLEADO` |
| `/api/reservas/**` | `GET`, `PUT`, `PATCH`, `DELETE` | `ADMINISTRADOR` y `EMPLEADO` |
| `/api/clientes`, `/api/clientes/**` | `GET`, `POST` | `ADMINISTRADOR` y `EMPLEADO` |

---

## 4. Flujos Operativos Críticos (Reglas de Negocio para el Front-end)

### 4.1. Regla de Persistencia Permanente y Eliminación de Clientes
1. **Nunca se borra un cliente automáticamente:** Cuando una reserva se cancela o termina, el cliente **permanece guardado en la base de datos** para futuros pedidos o reservas.
2. **Eliminación explícita por el Administrador (`DELETE /api/clientes/{id}`):**
   - Si el cliente tiene **reservas activas** (`PENDIENTE` o `CONFIRMADA`) o **pedidos/facturas contables**, el backend responderá `409 Conflict` explicando el motivo.
   - Si el cliente solo tenía reservas antiguas/canceladas o no tenía registros, el backend limpiará su historial de reservas inactivas, eliminará al cliente y responderá `200 OK` con:
     ```json
     {
       "mensaje": "Cliente eliminado exitosamente",
       "idCliente": 3
     }
     ```

### 4.2. Flujo de Pedidos (100% en Salón y Máquina de Estados Unidireccional)
1. **Prohibidos los pedidos para llevar:** Todo pedido exige obligatoriamente seleccionar una mesa (`numeroMesa`) y un cliente (`idCliente`).
2. **Descuento automático de stock y `subtotal` calculado en DTO:** Al crear el pedido (`POST /api/pedidos`), el backend verifica que cada producto tenga `disponibilidad = true` y `stock >= cantidad`, descuenta el inventario de inmediato, asigna `precio` en la entidad `DetallePedido` y calcula dinámicamente el **`subtotal`** del pedido (el cual no se guarda en la entidad `Pedido` ni en la base de datos, pero se mantiene en `PedidoResponseDTO` y `DetallePedidoResponseDTO` para enviarlo al Front-end). Además, cambia automáticamente la mesa a estado `OCUPADA`.
3. **Máquina de Estados Estrictamente Unidireccional (`PATCH /api/pedidos/{id}/estado`):**
   El Front-end solo debe permitir avanzar el estado hacia adelante siguiendo este orden:
   - `PENDIENTE` ➔ puede pasar a `EN_PREPARACION` (o `CANCELADO`).
   - `EN_PREPARACION` ➔ solo puede pasar a `ENTREGADO`.
   - `ENTREGADO` ➔ pasa automáticamente a `PAGADO` cuando se registra el pago en `/api/pagos`.
   - ⚠️ **Cualquier intento de retroceder un estado** (por ejemplo, pasar de `ENTREGADO` a `PENDIENTE` o `EN_PREPARACION`) devolverá `400 Bad Request`.

### 4.3. Flujo `Pago ➔ Factura` con Liquidación del 19% de IVA
1. **No existe `POST /api/facturas`:** El Front-end **nunca** crea una factura directamente. Las facturas nacen automáticamente al procesar el pago en **`POST /api/pagos`**.
2. **¿Cómo debe funcionar la pantalla de Cobro en el Front-end?**
   - Cuando el empleado selecciona un pedido listo para cobrar, el Front-end lee `pedido.subtotal` (ej. `$20,000.0`).
   - El Front-end muestra en pantalla el desglose financiero:
     - **Subtotal:** `pedido.subtotal` (ej. `20000.0`)
     - **Impuesto (IVA 19%):** `Math.round(subtotal * 0.19 * 100) / 100` (ej. `3800.0`)
     - **Propina voluntaria (`propina`):** Input numérico `>= 0` (ej. `2000.0`)
     - **Total a Pagar (`total`):** `subtotal + impuesto + propina` (ej. `25800.0`)
   - El Front-end permite seleccionar uno o varios **Métodos de Pago** mediante checkboxes o filas dinámicas (`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`) indicando cuánto abona en cada método.
   - **Validación de cuadre:** La suma de los montos de los métodos de pago seleccionados **debe ser exactamente igual al `total`** (`subtotal + 19% IVA + propina`). Si no cuadra, el backend responderá `422 Unprocessable Entity`.
   - Al enviar `POST /api/pagos`, el backend ejecuta en una sola transacción:
     1. Genera la factura con consecutivo `FAC-YYYY-XXXXX` en estado `PAGADA`.
     2. Registra los pagos en estado `PAGADO`.
     3. Cambia el estado del pedido a `PAGADO`.
     4. Libera la mesa a estado `DISPONIBLE`.
     5. Envía la factura electrónica HTML al correo del cliente de forma asíncrona.
     6. Devuelve `201 Created` con el objeto completo `FacturaResponseDTO` para que el Front-end muestre el comprobante en pantalla.

---

## 5. Catálogo de Endpoints Clave y Contratos JSON (Request / Response)

### 5.1. Categorías y Productos (Catálogo)
- **Clave Primaria de Categoría:** En la V2, la categoría se identifica por su nombre (`nombreCategoria` tipo `String`, ej. `"Bebidas Calientes"`).

#### Crear Categoría (`POST /api/categorias` — Rol `ADMINISTRADOR`)
**Request:**
```json
{
  "nombreCategoria": "Cafés Filtrados",
  "estado": "ACTIVA",
  "descripcion": "Preparaciones artesanales en Chemex, V60 y Prensa Francesa"
}
```
**Response (`201 Created`):**
```json
{
  "nombreCategoria": "Cafés Filtrados",
  "estado": "ACTIVA",
  "descripcion": "Preparaciones artesanales en Chemex, V60 y Prensa Francesa"
}
```

#### Crear Producto (`POST /api/productos` — Rol `ADMINISTRADOR`)
**Request:**
```json
{
  "nombre": "Café V60 Origen Huila",
  "descripcion": "Notas a panela, mandarina y chocolate oscuro",
  "precio": 10500.0,
  "disponibilidad": true,
  "stock": 40,
  "imagenUrl": "https://placehold.co/400x300/432818/F4F0EC?text=V60+Huila",
  "nombreCategoria": "Cafés Filtrados"
}
```
**Response (`201 Created`):**
```json
{
  "idProducto": 6,
  "nombre": "Café V60 Origen Huila",
  "descripcion": "Notas a panela, mandarina y chocolate oscuro",
  "precio": 10500.0,
  "disponibilidad": true,
  "stock": 40,
  "imagenUrl": "https://placehold.co/400x300/432818/F4F0EC?text=V60+Huila",
  "nombreCategoria": "Cafés Filtrados"
}
```

---

### 5.2. Reservas y Disponibilidad (`Público`, `EMPLEADO` y `ADMINISTRADOR`)

#### Consultar Mesas Disponibles (`GET /api/reservas/disponibles` — Público)
**URL de Ejemplo:**
```http
GET /api/reservas/disponibles?fechaHora=2026-10-25T19:30:00&numeroPersonas=4
```
**Response (`200 OK`):**
```json
[
  {
    "numeroMesa": 2,
    "capacidad": 4,
    "estado": "DISPONIBLE"
  },
  {
    "numeroMesa": 3,
    "capacidad": 4,
    "estado": "DISPONIBLE"
  }
]
```

#### Crear una Reserva (`POST /api/reservas` — Público)
El Front-end puede enviar `idReserva` e `idCliente` por separado en el DTO (o los datos del cliente `nombre`, `apellido`, `correo`, `telefono`), junto con `fecha` (`YYYY-MM-DD`) y `hora` (`HH:mm:ss`), o bien `fechaHora` (`YYYY-MM-DDTHH:mm:ss`). El backend une directamente ambos identificadores al crear el registro para guardarlos como una sola clave compuesta simple (`idReservaCompuesta` en formato `idReserva_idCliente`) en la entidad `Reserva`, sin crear clases adicionales:

**Request:**
```json
{
  "idReserva": 1,
  "idCliente": 3,
  "nombre": "Andrés",
  "apellido": "Silva",
  "correo": "andres.silva@gmail.com",
  "telefono": "3001112233",
  "numeroMesa": 2,
  "fecha": "2026-10-25",
  "hora": "19:30:00",
  "numeroPersonas": 4,
  "observaciones": "Mesa cerca de la ventana"
}
```
**Response (`201 Created`):**
```json
{
  "idReservaCompuesta": "1_3",
  "idReserva": 1,
  "idCliente": 3,
  "nombreCliente": "Andrés Silva",
  "correoCliente": "andres.silva@gmail.com",
  "numeroMesa": 2,
  "fecha": "2026-10-25",
  "hora": "19:30:00",
  "fechaHora": "2026-10-25T19:30:00",
  "numeroPersonas": 4,
  "estado": "PENDIENTE",
  "observaciones": "Mesa cerca de la ventana"
}
```

#### Listar Reservas de un Cliente por su Correo Único (`GET` — Público)
**Opción 1 (Path Variable):** `GET /api/reservas/correo/andres.silva@gmail.com`  
**Opción 2 (Query Param):** `GET /api/reservas?correo=andres.silva@gmail.com`

---

### 5.3. Pedidos en Salón (`Exclusivo Rol EMPLEADO` para creación y cambio de estado)

#### Crear un Pedido (`POST /api/pedidos` — Rol `EMPLEADO`)
**Request:**
```json
{
  "idCliente": 1,
  "numeroMesa": 1,
  "idUsuario": 2,
  "observaciones": "Leche deslactosada, sin azúcar",
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2
    },
    {
      "idProducto": 4,
      "cantidad": 1
    }
  ]
}
```
**Response (`201 Created`):**
```json
{
  "idPedido": 1,
  "idCliente": 1,
  "nombreCliente": "Mariana Ruiz",
  "numeroMesa": 1,
  "idUsuario": 2,
  "nombreUsuario": "Carlos Mesero",
  "fecha": "2026-10-18T19:00:00",
  "estado": "PENDIENTE",
  "subtotal": 21500.0,
  "observaciones": "Leche deslactosada, sin azúcar",
  "detalles": [
    {
      "idDetallePedido": 1,
      "idProducto": 1,
      "nombreProducto": "Café Espresso Doble",
      "cantidad": 2,
      "precioUnitario": 6500.0,
      "subtotal": 13000.0
    },
    {
      "idDetallePedido": 2,
      "idProducto": 4,
      "nombreProducto": "Croissant de Almendras",
      "cantidad": 1,
      "precioUnitario": 8500.0,
      "subtotal": 8500.0
    }
  ]
}
```

#### Actualizar Estado del Pedido (`PATCH /api/pedidos/{id}/estado` — Rol `EMPLEADO`)
Acepta `"nuevoEstado"` (o su alias `"estado"`) en el Body JSON, o bien por Query Param `?nuevoEstado=EN_PREPARACION`.

**Transición 1 (`PENDIENTE` ➔ `EN_PREPARACION`) — Request:**
```json
{
  "nuevoEstado": "EN_PREPARACION"
}
```
**Transición 2 (`EN_PREPARACION` ➔ `ENTREGADO`) — Request:**
```json
{
  "nuevoEstado": "ENTREGADO"
}
```
**Response (`200 OK`):** Devuelve el `PedidoResponseDTO` actualizado con el nuevo estado.

---

### 5.4. Procesar Pago y Generar Factura con 19% IVA (`POST /api/pagos` — Exclusivo Rol `EMPLEADO`)

Supongamos que el Pedido `#1` tiene `subtotal = 21500.0`:
- **Subtotal:** `21500.0`
- **Impuesto (19% IVA):** `21500.0 * 0.19 = 4085.0`
- **Propina voluntaria:** `2415.0`
- **Total Factura a cubrir:** `21500.0 + 4085.0 + 2415.0 = 28000.0`

El cliente decide pagar `$10,000` en `EFECTIVO` y `$18,000` con `TARJETA` (suma = `$28,000.0`):

**Request (`POST /api/pagos`):**
```json
{
  "idPedido": 1,
  "idUsuario": 2,
  "propina": 2415.0,
  "pagos": [
    {
      "tipoMetodoPago": "EFECTIVO",
      "total": 10000.0
    },
    {
      "tipoMetodoPago": "TARJETA",
      "total": 18000.0,
      "referenciaTransaccion": "VOUCHER-4451"
    }
  ]
}
```
*(Nota: En cada ítem de `pagos`, el monto puede enviarse como `"total"` o como su alias `"monto"`).*

**Response (`201 Created` — Devuelve la Factura Emitida y Pagada):**
```json
{
  "numFactura": "FAC-2026-00001",
  "idPedido": 1,
  "idCliente": 1,
  "nombreCliente": "Mariana Ruiz",
  "correoCliente": "mariana.ruiz@gmail.com",
  "telefonoCliente": "3109876543",
  "idUsuario": 2,
  "nombreUsuario": "Carlos Mesero",
  "fecha": "2026-10-18T19:15:30",
  "subtotal": 21500.0,
  "propina": 2415.0,
  "impuesto": 4085.0,
  "total": 28000.0,
  "estado": "PAGADA",
  "pagos": [
    {
      "idPago": 1,
      "numFactura": "FAC-2026-00001",
      "tipoMetodoPago": "EFECTIVO",
      "nombreMetodoPago": "Efectivo",
      "estado": "PAGADO",
      "total": 10000.0,
      "referenciaTransaccion": null,
      "fecha": "2026-10-18T19:15:30"
    },
    {
      "idPago": 2,
      "numFactura": "FAC-2026-00001",
      "tipoMetodoPago": "TARJETA",
      "nombreMetodoPago": "Tarjeta Débito / Crédito",
      "estado": "PAGADO",
      "total": 18000.0,
      "referenciaTransaccion": "VOUCHER-4451",
      "fecha": "2026-10-18T19:15:30"
    }
  ]
}
```

---

## 6. Estructura Estándar de Errores (`ErrorResponseDTO`)

El backend centraliza absolutamente todas las excepciones en `GlobalExceptionHandler` (`@RestControllerAdvice`). Siempre que ocurra un error (`400`, `401`, `403`, `404`, `409`, `422`, `500`), el Front-end recibirá esta misma estructura JSON para mostrar alertas o *Toasts*:

### 6.1. Error de Validación de Campos (`400 Bad Request`)
Ocurre cuando falla alguna anotación `@NotBlank`, `@NotNull`, `@Email`, `@Positive` o cuando se intenta retroceder un estado en la máquina de estados del pedido. Incluye el mapa `validationErrors` con el detalle exacto por campo:

```json
{
  "timestamp": "2026-10-18T19:20:10.125",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/clientes",
  "validationErrors": {
    "correo": "El formato del correo electrónico no es válido",
    "nombres": "Los nombres del cliente son obligatorios"
  }
}
```

### 6.2. Error de Autenticación (`401 Unauthorized`)
Ocurre cuando no se envía el header `Authorization: Bearer <token>`, el token expiró, las credenciales de login son incorrectas o el empleado está `INACTIVO`:

```json
{
  "timestamp": "2026-10-18T19:21:00.410",
  "status": 401,
  "error": "Unauthorized",
  "message": "Acceso no autorizado: Token JWT no proporcionado o inválido",
  "path": "/api/pedidos"
}
```

### 6.3. Error de Permisos de Rol (`403 Forbidden`)
Ocurre cuando un usuario autenticado intenta ejecutar una acción prohibida para su rol (ej. `ADMINISTRADOR` intentando crear un pedido o procesar un pago, o `EMPLEADO` intentando borrar un producto):

```json
{
  "timestamp": "2026-10-18T19:22:15.802",
  "status": 403,
  "error": "Forbidden",
  "message": "Acceso denegado: No tiene permisos para realizar esta operación",
  "path": "/api/pedidos"
}
```

### 6.4. Conflicto de Negocio o Duplicidad (`409 Conflict`)
Ocurre cuando se intenta registrar un correo/teléfono duplicado, reservar una mesa ya ocupada en ese horario, facturar un pedido que ya fue facturado, o eliminar un cliente que aún tiene reservas activas o facturas contables:

```json
{
  "timestamp": "2026-10-18T19:23:45.110",
  "status": 409,
  "error": "Conflict",
  "message": "No se puede eliminar el cliente con ID 1 porque posee historial de pedidos o facturas contables asociadas",
  "path": "/api/clientes/1",
  "validationErrors": null
}
```

### 6.5. Regla Operativa / Financiera No Procesable (`422 Unprocessable Entity`)
Ocurre cuando no hay stock suficiente para un producto (`StockInsuficienteException`) o cuando la suma de los pagos enviados en `POST /api/pagos` no coincide exactamente con `subtotal + 19% IVA + propina` (`PagoInvalidoException`):

```json
{
  "timestamp": "2026-10-18T19:25:02.990",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Descuadre de caja: la suma de los pagos recibidos (20000.00) no coincide con el total calculado de la factura con 19% IVA (28000.00)",
  "path": "/api/pagos",
  "validationErrors": null
}
```

---

## 7. Guía General Paso a Paso para Probar y Verificar el Backend (Cualquier Cliente HTTP o Front-end)

Si deseas probar el flujo completo del aplicativo desde cualquier cliente HTTP o durante el desarrollo del Front-end sin errores de token (`401`) ni de permisos (`403`), sigue este orden lógico:

### Paso 1: Configurar el Manejo de Tokens
Toda petición protegida necesita el header `Authorization: Bearer <TOKEN>`. Define dos tokens o sesiones de prueba:
1. **Obtener Token de Administrador (`adminToken`):**
   - Llama a `POST http://localhost:8080/api/auth/login` con:
     ```json
     { "correo": "admin@savanacoffee.com", "contrasena": "Password123" }
     ```
   - Guarda la propiedad `token` de la respuesta para todas las peticiones administrativas.
2. **Obtener Token de Empleado (`empleadoToken`):**
   - Llama a `POST http://localhost:8080/api/auth/login` con:
     ```json
     { "correo": "empleado@savanacoffee.com", "contrasena": "Password123" }
     ```
   - Guarda la propiedad `token` de la respuesta para las peticiones operativas de salón (Pedidos y Pagos).

### Paso 2: Recorrido Completo de Verificación (End-to-End)

1. **Flujo Público (Sin enviar header `Authorization`):**
   - Consulta las categorías activas en `GET /api/categorias?estado=ACTIVA`.
   - Consulta los productos disponibles en `GET /api/productos?disponibilidad=true`.
   - Verifica las mesas libres en `GET /api/reservas/disponibles?fechaHora=2026-10-25T19:30:00&numeroPersonas=4`.
   - Crea una reserva en `POST /api/reservas` enviando los datos del cliente (`nombres`, `apellidos`, `correo`, `telefono`, `numeroMesa`, `fecha`, `hora`, `numeroPersonas`).
   - Consulta las reservas de ese cliente por su correo único en `GET /api/reservas/correo/{correo}`.

2. **Flujo de Administrador (Enviando `Authorization: Bearer <adminToken>`):**
   - Registra un nuevo empleado en `POST /api/usuarios` con `"rol": "EMPLEADO"` y `"estado": "ACTIVO"`.
   - Prueba inactivar y reactivar a ese empleado en `PATCH /api/usuarios/{id}/estado?estado=INACTIVO` y luego `ACTIVO`.
   - Crea una nueva categoría en `POST /api/categorias` y un nuevo producto asociado a esa categoría en `POST /api/productos`.
   - Consulta los indicadores del negocio en `GET /api/dashboard/resumen`.

3. **Flujo Operativo de Empleado (Enviando `Authorization: Bearer <empleadoToken>`):**
   - Crea un pedido en mesa en `POST /api/pedidos` indicando `idCliente`, `numeroMesa`, `idUsuario` y la lista `detalles`. Observa el `subtotal` devuelto en la respuesta.
   - Avanza el pedido a preparación con `PATCH /api/pedidos/{id}/estado` enviando `{ "nuevoEstado": "EN_PREPARACION" }`.
   - Avanza el pedido a entregado con `PATCH /api/pedidos/{id}/estado` enviando `{ "nuevoEstado": "ENTREGADO" }`.
   - Calcula el total a cobrar (`subtotal + (subtotal * 0.19) + propina`) y procesa el cobro en `POST /api/pagos` enviando el arreglo `pagos` con los métodos seleccionados (`EFECTIVO`, `TARJETA` o `TRANSFERENCIA`).
   - Verifica la factura generada (`FAC-YYYY-XXXXX`) en la respuesta del pago o consultándola en `GET /api/facturas/{numFactura}` y comprueba que la mesa quedó nuevamente en estado `DISPONIBLE` (`GET /api/mesas/{numeroMesa}`).
