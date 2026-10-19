# ☕ Savana Data Coffee - API Backend

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-SQL_Nativo-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Spring Security JWT](https://img.shields.io/badge/JWT-Stateless_Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![OpenAPI 3](https://img.shields.io/badge/OpenAPI_3-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

---

## 📋 Descripción General

**Savana Data Coffee - API Backend** es un sistema empresarial de gestión operativa, contable y administrativa diseñado para la operación integral de una cafetería de especialidad. La plataforma centraliza el directorio permanente de clientes (sin requerir contraseña ni cuenta de usuario), la administración del catálogo de productos y categorías, el control de mesas físicas, la programación de reservas con validación de solapamientos horarios, la toma de comandas 100% en salón con descuento atómico de inventario y un motor transaccional de facturación impulsado por pagos múltiples que liquida automáticamente el **19% de IVA** y despacha comprobantes electrónicos en formato HTML de manera asíncrona.

---

## ✨ Características Principales

- **Persistencia 100% en SQL Nativo Puro (`JdbcTemplate`):** Cumplimiento estricto de cero uso de ORMs (prohibición total de JPA/Hibernate). Todas las operaciones CRUD, `JOIN`s jerárquicos, agregaciones analíticas e inserciones por lote (`batchUpdate`) se ejecutan mediante sentencias SQL nativas optimizadas.
- **Seguridad Stateless con JWT y 2FA (OTP en Memoria):** Autenticación basada en tokens firmados con HMAC-SHA256 (`jjwt`) y verificación en dos pasos (2FA) gestionada mediante una estructura concurrente thread-safe (`ConcurrentHashMap`) con vigencia de 5 minutos, sin contaminar la base de datos con códigos temporales.
- **Jerarquía de Personal (`Staff`) y Matriz Estricta de Roles:** Separación relacional 1:1 entre `STAFF`, `ADMINISTRADOR` y `EMPLEADO` con control de acceso granular en `SecurityConfig`:
  - **Público (`Cliente`):** Consulta de menú (`GET /api/productos`, `GET /api/categorias`), verificación de disponibilidad de mesas (`GET /api/reservas/disponibles`), creación de reservas (`POST /api/reservas`) con persistencia permanente del cliente y consulta de reservas históricas mediante su correo único (`GET /api/reservas/correo/{correo}`).
  - **`ROLE_ADMINISTRADOR`:** Gestión CRUD de empleados (`Staff`), administración del catálogo (categorías y productos), control de mesas, eliminación explícita controlada de clientes (`DELETE /api/clientes/{id}`) y auditoría operativa/financiera en el dashboard (`GET /api/dashboard/resumen`). Tiene estrictamente prohibido (`403 Forbidden`) tomar pedidos o procesar pagos.
  - **`ROLE_EMPLEADO`:** Operación exclusiva de salón. Registra comandas en mesa (`POST /api/pedidos`), controla la máquina de estados unidireccional del pedido (`PENDIENTE` ➔ `EN_PREPARACION` ➔ `ENTREGADO` ➔ `PAGADO`), habilita/ocupa mesas y procesa cobros (`POST /api/pagos`). Tiene prohibido gestionar personal o modificar el catálogo.
- **Comandas en Salón y Control Atómico de Inventario:** Validación en tiempo real de disponibilidad y existencias por producto, descuento atómico de stock y congelamiento de precios unitarios en `detalle_pedido_contener` para garantizar integridad contable histórica.
- **Motor Transaccional `Pago ➔ Factura` con IVA del 19%:** Al procesar el pago (`POST /api/pagos`), el sistema soporta pagos simples o mixtos (`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`), calcula automáticamente el impuesto del **19% de IVA** sobre el subtotal del pedido (`total = subtotal + impuesto + propina`), verifica el cuadre exacto de caja, emite la factura con consecutivo `FAC-YYYY-XXXXX`, marca el pedido como `PAGADO` y libera la mesa a estado `DISPONIBLE`.
- **Notificaciones Transaccionales Asíncronas (`@Async`):** Envío no bloqueante de correos electrónicos con plantillas HTML corporativas para códigos OTP 2FA, bienvenida/baja de personal, confirmación/cancelación de reservas y comprobantes de facturación electrónica.

---

## 🛠️ Stack Tecnológico

| Capa / Propósito | Tecnología / Librería |
| :--- | :--- |
| **Lenguaje & Runtime** | Java 21 (LTS) |
| **Framework Backend** | Spring Boot 3.x (`spring-boot-starter-web`, `spring-boot-starter-jdbc`) |
| **Motor de Base de Datos** | PostgreSQL (Producción) / H2 Database en `MODE=PostgreSQL` (Desarrollo/Testing) |
| **Acceso a Datos** | Spring JDBC (`JdbcTemplate`, `PreparedStatementCreator`, `GeneratedKeyHolder`) |
| **Seguridad & Criptografía** | Spring Security 6, JSON Web Tokens (`io.jsonwebtoken:jjwt`), `BCryptPasswordEncoder` |
| **Mapeo de Objetos & DTOs** | MapStruct (`org.mapstruct:mapstruct`), Project Lombok (`@Data`, `@Builder`, `@SuperBuilder`) |
| **Validación de Datos** | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| **Mensajería & Correo** | Spring Boot Starter Mail (`JavaMailSender`, `MimeMessageHelper`, `@EnableAsync`) |
| **Documentación de API** | Springdoc OpenAPI 3 / Swagger UI (`springdoc-openapi-starter-webmvc-ui`) |
| **Testing** | JUnit 5 (Jupiter), Mockito, Spring Boot Test |
| **Construcción & Dependencias** | Apache Maven (`mvnw`) |

---

## 📂 Arquitectura del Proyecto

El código fuente sigue una arquitectura en capas limpia, modular y desacoplada bajo el paquete base `org.sabana.proyectofinal_bd1_back`:

```text
org.sabana.proyectofinal_bd1_back/
│
├── config/           # Configuración de Seguridad (SecurityConfig), CORS, OpenAPI 3 (OpenApiConfig).
├── controller/       # Controladores REST (@RestController) que exponen la API y validan payloads (@Valid).
├── model/            # Modelos de dominio y transferencia de datos.
│   ├── entity/       # POJOs puros que representan las tablas relacionales (Cero anotaciones JPA).
│   └── dto/          # RequestDTO, ResponseDTO e interfaces @Mapper de MapStruct.
├── repository/       # Contratos e implementaciones DAO (@Repository) con SQL nativo puro y JdbcTemplate.
├── security/         # Infraestructura JWT (JwtTokenProvider y filtro stateless JwtAuthenticationFilter).
├── service/          # Lógica de negocio (@Service), reglas transaccionales (@Transactional) y caché OTP 2FA.
├── util/             # Componentes transversales del sistema.
│   ├── email/        # Servicio de correo asíncrono (@Async EmailServiceImpl) y EmailTemplateBuilder HTML.
│   └── exceptions/   # Excepciones de dominio y manejo centralizado (@RestControllerAdvice GlobalExceptionHandler).
└── ProyectoFinalBd1BackApplication.java
```

---

## ⚙️ Prerrequisitos

Antes de clonar y ejecutar el proyecto, asegúrate de contar con las siguientes herramientas instaladas en tu entorno:

- **Java Development Kit (JDK):** Versión **21** o superior (`java -version`).
- **Apache Maven:** Versión **3.9+** (o utilizar el wrapper `./mvnw` incluido en el repositorio).
- **PostgreSQL:** Versión **15+** (opcional para entorno local si se ejecuta con el perfil H2 en modo compatibilidad PostgreSQL).

---

## 🚀 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-organizacion/ProyectoFinal_BD1_BACK.git
cd ProyectoFinal_BD1_BACK
```

### 2. Configuración de Variables de Entorno (`application.properties`)

Configura las credenciales de conexión a base de datos, firma JWT y servidor SMTP en `src/main/resources/application.properties` o mediante variables de entorno del sistema operativo:

```properties
# ==========================================
# SERVIDOR
# ==========================================
server.port=8080

# ==========================================
# BASE DE DATOS (PostgreSQL en Producción)
# ==========================================
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:savana_coffee}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# Inicialización del esquema relacional V2
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql

# ==========================================
# SEGURIDAD JWT
# ==========================================
jwt.secret=${JWT_SECRET:SavanaDataCoffeeSuperSecretSecurityKey2026Secure256BitsLongEnough}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

# ==========================================
# NOTIFICACIONES POR CORREO (SMTP)
# ==========================================
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:tu_correo@gmail.com}
spring.mail.password=${MAIL_PASSWORD:tu_app_password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Nota para Desarrollo Local Rápido:** El proyecto incluye soporte preconfigurado para **H2 Database en modo `MODE=PostgreSQL`**, el cual inicializa automáticamente las 13 tablas del modelo relacional y carga datos semilla (`schema.sql`) sin requerir una instancia externa de PostgreSQL activa.

### 3. Compilar y Ejecutar la Aplicación

Usando Maven instalado globalmente:

```bash
mvn clean spring-boot:run
```

O utilizando el Maven Wrapper incluido en el proyecto:

```bash
# En Linux / macOS
./mvnw spring-boot:run

# En Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run
```

### 4. Ejecutar la Suite de Pruebas Unitarias

```bash
mvn test
```

---

## 📖 Documentación de la API (Swagger UI)

Una vez que el servidor esté en ejecución sobre el puerto `8080`, puedes explorar y probar interactivamente todos los endpoints REST a través de **Swagger UI**:

- **Swagger UI Interactivo:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Especificación OpenAPI v3 (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Consola H2 (Entorno Local):** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

### Credenciales Semilla para Pruebas (`POST /api/auth/login`)

| Rol | Correo Electrónico | Contraseña | Permisos Principales |
| :--- | :--- | :--- | :--- |
| **`ROLE_ADMINISTRADOR`** | `admin@savanacoffee.com` | `Password123` | Gestión de Staff, Catálogo, Mesas, Clientes y Dashboard. |
| **`ROLE_EMPLEADO`** | `empleado@savanacoffee.com` | `Password123` | Toma de Pedidos en Mesa, Cambio de Estados y Procesamiento de Pagos/Facturas. |

Para autenticar tus peticiones en Swagger UI:
1. Ejecuta `POST /api/auth/login` con cualquiera de las cuentas anteriores.
2. Copia el valor de la propiedad `"token"` devuelta en el JSON de respuesta.
3. Haz clic en el botón **`Authorize 🔒`** en la esquina superior derecha de Swagger UI, pega el token JWT y confirma.

---

## 👥 Autores / Equipo de Desarrollo

Desarrollado por el equipo **"Las Chic@s Super Poderos@s"**:

- **Anamaria Moreno** — *Backend Developer & Database Architect*
- **Valentina Villegas** — *Backend Developer & QA Engineer*
- **Santiago Monroy** — *Backend Developer & Security/Architecture Lead*
