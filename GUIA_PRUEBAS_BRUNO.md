# ☕ Guía Rápida para Probar la API de Savana Data Coffee en Bruno

Esta guía explica paso a paso cómo levantar el backend, configurar **Bruno** (incluyendo el token JWT automático) y probar todos los módulos de la API en orden lógico.

---

## 1. Levantar el Servidor Backend

En la terminal, desde la raíz del proyecto, ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

* **URL Base de la API:** `http://localhost:8080`
* **Swagger UI (Documentación interactiva):** `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON (Para importar en Bruno):** `http://localhost:8080/v3/api-docs`
* **Consola H2 (Base de datos local):** `http://localhost:8080/h2-console`
  * *JDBC URL:* `jdbc:h2:file:./data/savanadb`
  * *User Name:* `sa` (sin contraseña)

---

## 2. Cómo Configurar Bruno en 2 Minutos

### Opción A: Importar todas las rutas automáticamente (¡Recomendado!)
1. Con el backend encendido, abre **Bruno**.
2. Haz clic en **Import Collection** -> selecciona **OpenAPI V3**.
3. Pega la URL: `http://localhost:8080/v3/api-docs` (o descárgala desde el navegador y súbela como archivo).
4. ¡Listo! Bruno creará todas las carpetas y peticiones automáticamente.

### Opción B: Crear tu Colección Manualmente y Configurar el Entorno
1. Crea una nueva colección llamada **`Savana Data Coffee`**.
2. Arriba a la derecha en Bruno, haz clic en **No Environment** -> **Configure** -> **Create Environment** (llámalo `Local`).
3. Agrega dos variables:
   * `baseUrl` = `http://localhost:8080`
   * `token` = *(déjalo vacío por ahora)*
4. Haz clic derecho sobre tu colección -> **Collection Settings** -> pestaña **Auth**:
   * Selecciona **Bearer Token**.
   * En el campo Token escribe: `{{token}}` y guarda.
   * Ahora todas tus peticiones que tengan Auth en modo *Inherit* usarán el token automáticamente.

---

## 3. Paso 1: Autenticación y Obtención del Token JWT (Rutas Públicas)

Las rutas `/api/auth/**` son **públicas** (no requieren token). Empieza por aquí para obtener tu JWT:

### 3.1 Registrar tu propio Usuario Administrador (`POST`)
> 💡 **Tip:** Usa tu correo real si quieres ver llegar el correo HTML de bienvenida y los códigos 2FA. Si pones `dosFactoresHabilitado: false`, el login te dará el JWT directo. Si pones `true`, te enviará un código OTP de 6 dígitos por correo.

* **Método:** `POST`
* **URL:** `{{baseUrl}}/api/auth/registro`
* **Body -> JSON:**
```json
{
  "nombre": "Admin Savana",
  "email": "tu_correo@gmail.com",
  "password": "Password123*",
  "rol": "ADMINISTRADOR",
  "dosFactoresHabilitado": false
}
```

### 3.2 Iniciar Sesión (`POST`)
* **Método:** `POST`
* **URL:** `{{baseUrl}}/api/auth/login`
* **Body -> JSON:**
```json
{
  "email": "tu_correo@gmail.com",
  "password": "Password123*"
}
```
* **Truco Pro en Bruno (Pestaña `Script` -> `Post Response`):**
  Pega esto en la pestaña **Script -> Post Response** de tu petición de Login para que Bruno guarde el token automáticamente cada vez que inicies sesión:
  ```javascript
  if (res.body && res.body.token) {
    bru.setEnvVar("token", res.body.token);
  }
  ```

### 3.3 Verificar 2FA (`POST` - Solo si activaste `dosFactoresHabilitado: true`)
Si activaste 2FA, revisa tu bandeja de correo (o la consola de Spring Boot si estás sin internet) y envía el código de 6 dígitos:
* **Método:** `POST`
* **URL:** `{{baseUrl}}/api/auth/verificar-2fa`
* **Body -> JSON:**
```json
{
  "email": "tu_correo@gmail.com",
  "codigoOtp": "123456"
}
```

---

## 4. Catálogo Completo de Rutas para Empezar a Probar

> ⚠️ **Recuerda:** Todas las rutas siguientes requieren el header `Authorization: Bearer {{token}}` (en Bruno: pestaña **Auth** -> **Bearer Token**).
> La base de datos ya trae **datos semilla precargados** (Clientes ID `1` y `2`, Mesas ID `1` a `5`, Categorías ID `1` a `4`, Productos ID `1` a `5`, Métodos de Pago ID `1` a `4`).

---

### 📊 A. Dashboard Operativo (`/api/dashboard`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/dashboard/resumen` | Obtiene métricas en tiempo real (productos activos, mesas libres, pedidos en curso, ventas de hoy y reservas de hoy). |

---

### 👥 B. Clientes (`/api/clientes`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/clientes` | Lista todos los clientes registrados. |
| `GET` | `{{baseUrl}}/api/clientes/1` | Busca un cliente por su ID. |
| `POST` | `{{baseUrl}}/api/clientes` | Crea un nuevo cliente. |
| `PUT` | `{{baseUrl}}/api/clientes/1` | Actualiza los datos de un cliente. |
| `DELETE` | `{{baseUrl}}/api/clientes/1` | Elimina un cliente por su ID. |

**Ejemplo Body `POST {{baseUrl}}/api/clientes`:**
```json
{
  "nombre": "Carlos Mendoza",
  "telefono": "3001234567",
  "email": "tu_correo@gmail.com"
}
```

---

### 🪑 C. Mesas (`/api/mesas`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/mesas` | Lista todas las mesas. |
| `GET` | `{{baseUrl}}/api/mesas?estado=DISPONIBLE` | Filtra mesas por estado (`DISPONIBLE`, `OCUPADA`, `RESERVADA`, `EN_MANTENIMIENTO`). |
| `GET` | `{{baseUrl}}/api/mesas/1` | Obtiene una mesa por su ID. |
| `POST` | `{{baseUrl}}/api/mesas` | Registra una nueva mesa. |
| `PUT` | `{{baseUrl}}/api/mesas/1` | Actualiza número, capacidad o estado de una mesa. |
| `DELETE` | `{{baseUrl}}/api/mesas/5` | Elimina una mesa. |

**Ejemplo Body `POST {{baseUrl}}/api/mesas`:**
```json
{
  "numero": 6,
  "capacidad": 4,
  "estado": "DISPONIBLE"
}
```

---

### 🗂️ D. Categorías (`/api/categorias`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/categorias` | Lista todas las categorías. |
| `GET` | `{{baseUrl}}/api/categorias?soloActivas=true` | Lista únicamente las categorías activas. |
| `GET` | `{{baseUrl}}/api/categorias/1` | Busca categoría por ID. |
| `POST` | `{{baseUrl}}/api/categorias` | Crea una nueva categoría. |
| `PUT` | `{{baseUrl}}/api/categorias/1` | Actualiza una categoría. |
| `DELETE` | `{{baseUrl}}/api/categorias/1` | Elimina una categoría. |

**Ejemplo Body `POST {{baseUrl}}/api/categorias`:**
```json
{
  "nombre": "Métodos Filtrados",
  "descripcion": "Chemex, V60 y Aeropress con cafés de origen",
  "activo": true
}
```

---

### 🥐 E. Productos (`/api/productos`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/productos` | Lista todo el catálogo de productos (incluye nombre de categoría). |
| `GET` | `{{baseUrl}}/api/productos?categoriaId=1&disponible=true` | Filtra por categoría y/o disponibilidad. |
| `GET` | `{{baseUrl}}/api/productos/1` | Consulta un producto por ID. |
| `POST` | `{{baseUrl}}/api/productos` | Crea un nuevo producto. |
| `PUT` | `{{baseUrl}}/api/productos/1` | Actualiza un producto existente. |
| `DELETE` | `{{baseUrl}}/api/productos/1` | Elimina un producto. |

**Ejemplo Body `POST {{baseUrl}}/api/productos`:**
```json
{
  "nombre": "Latte Vainilla Artesanal",
  "descripcion": "Espresso doble con leche cremada y extracto natural de vainilla",
  "precio": 10500.0,
  "idCategoria": 1,
  "imagenUrl": "https://placehold.co/400x300/7F5539/F4F0EC?text=Latte",
  "disponible": true,
  "stock": 50
}
```

---

### 📅 F. Reservas (`/api/reservas`) — *Dispara Correos HTML de Confirmación y Cancelación*

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/reservas` | Lista reservas (filtros opcionales: `?clienteId=1`, `?mesaId=2`, `?estado=PENDIENTE`). |
| `GET` | `{{baseUrl}}/api/reservas/disponibles?fechaHora=2026-10-25T19:00:00&numeroPersonas=4` | Consulta qué mesas están disponibles para una fecha/hora futura y cantidad de personas. |
| `GET` | `{{baseUrl}}/api/reservas/1` | Busca una reserva por su ID. |
| `POST` | `{{baseUrl}}/api/reservas` | Crea una reserva (envía correo HTML de confirmación al cliente). |
| `PUT` | `{{baseUrl}}/api/reservas/1` | Actualiza una reserva. |
| `PATCH` | `{{baseUrl}}/api/reservas/1/estado?estado=CANCELADA` | Cambia el estado (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `COMPLETADA`). Si es `CANCELADA`, envía correo de liberación de cupo. |
| `DELETE` | `{{baseUrl}}/api/reservas/1` | Elimina la reserva y envía correo de cancelación. |

**Ejemplo Body `POST {{baseUrl}}/api/reservas`:**
```json
{
  "idCliente": 1,
  "idMesa": 2,
  "fechaHora": "2026-10-25T19:00:00",
  "numeroPersonas": 4,
  "estado": "CONFIRMADA",
  "observaciones": "Mesa cerca de la ventana"
}
```

---

### 🧾 G. Pedidos / Comandas (`/api/pedidos`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/pedidos` | Lista todos los pedidos con sus ítems (filtros opcionales: `?clienteId=1`, `?estado=PENDIENTE`). |
| `GET` | `{{baseUrl}}/api/pedidos/1` | Obtiene el pedido y su desglose de productos por ID. |
| `POST` | `{{baseUrl}}/api/pedidos` | Crea un pedido, congela precios unitarios, calcula el total y descuenta stock automáticamente. |
| `PATCH` | `{{baseUrl}}/api/pedidos/1/estado` | Actualiza el estado (`PENDIENTE`, `EN_PREPARACION`, `ENTREGADO`, `CANCELADO`, `PAGADO`). |
| `DELETE` | `{{baseUrl}}/api/pedidos/1` | Elimina un pedido. |

**Ejemplo Body `POST {{baseUrl}}/api/pedidos`:**
*(Nota: Producto 1 cuesta `$6,500` x 2 = `$13,000`; Producto 4 cuesta `$8,500` x 1 = `$8,500` -> Total del pedido = `$21,500`)*
```json
{
  "idCliente": 1,
  "idMesa": 2,
  "idUsuario": 1,
  "tipoPedido": "MESA",
  "observaciones": "Cappuccino bien caliente, sin azúcar",
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

**Ejemplo Body `PATCH {{baseUrl}}/api/pedidos/1/estado`:**
```json
{
  "estado": "ENTREGADO"
}
```

---

### 💳 H. Métodos de Pago (`/api/metodos-pago`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/metodos-pago` | Lista todos los medios de pago (`?soloActivos=true` opcional). |
| `GET` | `{{baseUrl}}/api/metodos-pago/1` | Busca un método de pago por ID. |
| `POST` | `{{baseUrl}}/api/metodos-pago` | Crea un nuevo método de pago. |
| `PUT` | `{{baseUrl}}/api/metodos-pago/1` | Actualiza un método de pago. |
| `DELETE` | `{{baseUrl}}/api/metodos-pago/4` | Elimina un método de pago. |

**Ejemplo Body `POST {{baseUrl}}/api/metodos-pago`:**
```json
{
  "codigo": "BONO_REGALO",
  "nombre": "Bono Regalo Savana",
  "descripcion": "Tarjeta de regalo prepagada de la cafetería",
  "activo": true
}
```

---

### 💰 I. Facturación y Cierre de Cuenta (`/api/facturas`) — *Dispara Factura Electrónica HTML por Correo*

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `{{baseUrl}}/api/facturas` | Liquida el pedido (Subtotal + 8% Impuesto + Propina - Descuento), registra pagos simples o mixtos, pasa el pedido a `PAGADO`, libera la mesa a `DISPONIBLE` y envía la factura HTML por correo al cliente. |
| `GET` | `{{baseUrl}}/api/facturas` | Lista facturas emitidas (filtros opcionales: `?clienteId=1&fechaInicio=2026-10-01T00:00:00&fechaFin=2026-10-31T23:59:59`). |
| `GET` | `{{baseUrl}}/api/facturas/1` | Obtiene una factura con sus pagos por ID. |
| `GET` | `{{baseUrl}}/api/facturas/pedido/1` | Obtiene la factura asociada a un `idPedido`. |
| `PATCH` | `{{baseUrl}}/api/facturas/1/anular` | Anula una factura emitida (`estado = ANULADA`). |

**Cálculo para probar `POST {{baseUrl}}/api/facturas` con el Pedido #1 del ejemplo anterior:**
* Subtotal del Pedido #1 = `21500.0`
* Impuesto (8% de 21500) = `1720.0`
* Propina voluntaria = `2000.0`
* Descuento = `0.0`
* **Total a Pagar** = `21500 + 1720 + 2000 - 0` = **`25220.0`** (La suma de `pagos` debe coincidir exactamente con este total):

```json
{
  "idPedido": 1,
  "idUsuario": 1,
  "propina": 2000.0,
  "descuento": 0.0,
  "pagos": [
    {
      "idMetodoPago": 1,
      "monto": 15220.0,
      "referenciaTransaccion": "CAJA-EFECTIVO-01"
    },
    {
      "idMetodoPago": 2,
      "monto": 10000.0,
      "referenciaTransaccion": "VOUCHER-998877"
    }
  ]
}
```

---

### 🔐 J. Administración de Usuarios (`/api/usuarios` - Requiere Rol `ADMINISTRADOR`)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `{{baseUrl}}/api/usuarios` | Lista todos los usuarios del sistema. |
| `GET` | `{{baseUrl}}/api/usuarios/1` | Busca un usuario por ID. |
| `PUT` | `{{baseUrl}}/api/usuarios/2` | Actualiza datos o rol de un usuario. |
| `DELETE` | `{{baseUrl}}/api/usuarios/3` | Elimina un usuario y dispara correo HTML de desactivación de cuenta. |
