-- ====================================================================
-- SAVANA DATA COFFEE - SCRIPT DE INICIALIZACIÓN V3 (POSTGRESQL / H2)
-- Basado en el Diagrama Oficial Entidad-Relación en Mermaid (MERE)
-- ====================================================================

DROP TABLE IF EXISTS pago CASCADE;
DROP TABLE IF EXISTS pago_factura CASCADE;
DROP TABLE IF EXISTS factura CASCADE;
DROP TABLE IF EXISTS detalle_pedido_contener CASCADE;
DROP TABLE IF EXISTS detalle_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS reserva CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS mesa CASCADE;
DROP TABLE IF EXISTS mesero CASCADE;
DROP TABLE IF EXISTS administrador CASCADE;
DROP TABLE IF EXISTS staff CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS metodo_pago CASCADE;

-- 1. TABLA: cliente (Comensales - Persistencia Permanente, correo y teléfono únicos)
CREATE TABLE cliente (
    id_cliente BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL DEFAULT '',
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL UNIQUE
);

-- 2. TABLA PADRE: staff (Jerarquía de personal sin columnas OTP en base de datos)
CREATE TABLE staff (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL DEFAULT '',
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    dos_fac_hab BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 3. TABLA HIJA 1:1: administrador (Hereda de staff)
CREATE TABLE administrador (
    id_usuario BIGINT PRIMARY KEY,
    CONSTRAINT fk_admin_staff FOREIGN KEY (id_usuario) REFERENCES staff(id_usuario) ON DELETE CASCADE
);

-- 4. TABLA HIJA 1:1: mesero (Hereda de staff con estado ACTIVO / INACTIVO)
CREATE TABLE mesero (
    id_usuario BIGINT PRIMARY KEY,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, INACTIVO
    CONSTRAINT fk_mesero_staff FOREIGN KEY (id_usuario) REFERENCES staff(id_usuario) ON DELETE CASCADE
);

-- 5. TABLA: mesa (Salón y capacidad, PK por numero_mesa)
CREATE TABLE mesa (
    numero_mesa INT PRIMARY KEY,
    capacidad INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE' -- DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO
);

-- 6. TABLA: categoria (Catálogo de productos con nombre_categoria como PK)
CREATE TABLE categoria (
    nombre_categoria VARCHAR(100) PRIMARY KEY,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, INACTIVA
    descripcion TEXT
);

-- 7. TABLA: producto (Bebidas, comidas y repostería)
CREATE TABLE producto (
    id_producto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio DOUBLE PRECISION NOT NULL,
    disponibilidad BOOLEAN NOT NULL DEFAULT TRUE,
    stock INT NOT NULL DEFAULT 0,
    imagen_url VARCHAR(255) NOT NULL,
    nombre_categoria VARCHAR(100) NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (nombre_categoria) REFERENCES categoria(nombre_categoria) ON UPDATE CASCADE
);

-- 8. TABLA: reserva (Gestión de citas y mesas con fecha y hora)
CREATE TABLE reserva (
    id_reserva BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    numero_mesa INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    numero_personas INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
    observaciones TEXT,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_reserva_mesa FOREIGN KEY (numero_mesa) REFERENCES mesa(numero_mesa)
);

-- 9. TABLA: pedido (Comandas 100% en salón asociadas a mesa, cliente y staff; sin subtotal persistido)
CREATE TABLE pedido (
    id_pedido BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    numero_mesa INT NOT NULL,
    id_usuario BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_PREPARACION, ENTREGADO, PAGADO, CANCELADO
    observaciones TEXT,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_pedido_mesa FOREIGN KEY (numero_mesa) REFERENCES mesa(numero_mesa),
    CONSTRAINT fk_pedido_staff FOREIGN KEY (id_usuario) REFERENCES staff(id_usuario)
);

-- 10. TABLA: detalle_pedido (Relación N:M contener entre Pedido y Producto con clave compuesta)
CREATE TABLE detalle_pedido (
    id_pedido BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (id_pedido, id_producto),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- 11. TABLA: metodo_pago (Medios de pago soportados con atributo único tipo como PK)
CREATE TABLE metodo_pago (
    tipo VARCHAR(50) PRIMARY KEY -- EFECTIVO, TARJETA, TRANSFERENCIA
);

-- 12. TABLA: factura (Comprobante fiscal 1:1 con pedido, IVA 19%, sin subtotal, id_cliente ni id_usuario)
CREATE TABLE factura (
    num_factura VARCHAR(50) PRIMARY KEY,
    id_pedido BIGINT NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PAGADA', -- EMITIDA, PAGADA
    propina DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    impuesto DOUBLE PRECISION NOT NULL DEFAULT 0.0, -- 19% IVA
    total DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_factura_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
);

-- 13. TABLA: pago (Pagos asociados a una factura y a un metodo_pago con monto y sin referencia)
CREATE TABLE pago (
    id_pago BIGSERIAL PRIMARY KEY,
    num_factura VARCHAR(50) NOT NULL,
    tipo_metodo_pago VARCHAR(50) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PAGADO', -- PENDIENTE, PAGADO
    monto DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_pago_factura FOREIGN KEY (num_factura) REFERENCES factura(num_factura) ON DELETE CASCADE,
    CONSTRAINT fk_pago_metodo FOREIGN KEY (tipo_metodo_pago) REFERENCES metodo_pago(tipo)
);

-- ====================================================================
-- DATOS INICIALES (SEMILLA) PARA PRUEBAS INMEDIATAS
-- Contraseña encriptada para todos los miembros de staff: Password123
-- ====================================================================

-- 1. Staff y Herencia (Administrador y Meseros)
INSERT INTO staff (nombres, apellidos, correo, contrasena, dos_fac_hab, fecha_inicio) VALUES
    ('Administrador', 'General', 'admin@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', false, CURRENT_DATE),
    ('Carlos', 'Mesero', 'mesero@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', false, CURRENT_DATE),
    ('Luisa', 'Operativa', 'luisa@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', false, CURRENT_DATE);

INSERT INTO administrador (id_usuario) VALUES (1);

INSERT INTO mesero (id_usuario, estado) VALUES
    (2, 'ACTIVO'),
    (3, 'ACTIVO');

-- 2. Clientes de Ejemplo (Persistencia Permanente)
INSERT INTO cliente (nombre, apellido, correo, telefono) VALUES
    ('Mariana', 'Ruiz', 'mariana.ruiz@gmail.com', '3109876543'),
    ('Juan', 'Perez', 'juan.perez@hotmail.com', '3151234567');

-- 3. Mesas del Salón
INSERT INTO mesa (numero_mesa, capacidad, estado) VALUES
    (1, 2, 'DISPONIBLE'),
    (2, 4, 'DISPONIBLE'),
    (3, 4, 'DISPONIBLE'),
    (4, 6, 'DISPONIBLE'),
    (5, 8, 'DISPONIBLE');

-- 4. Métodos de Pago Oficiales (Atributo único tipo)
INSERT INTO metodo_pago (tipo) VALUES
    ('EFECTIVO'),
    ('TARJETA'),
    ('TRANSFERENCIA');

-- 5. Categorías del Menú (PK nombre_categoria)
INSERT INTO categoria (nombre_categoria, estado, descripcion) VALUES
    ('Bebidas Calientes', 'ACTIVA', 'Cafés de especialidad, espressos y lattes'),
    ('Bebidas Frías', 'ACTIVA', 'Frappés, cold brew y sodas saborizadas'),
    ('Repostería y Panadería', 'ACTIVA', 'Pastelería fresca horneada a diario'),
    ('Almuerzos Ligeros', 'ACTIVA', 'Sandwiches artesanales y ensaladas');

-- 6. Productos (FK nombre_categoria)
INSERT INTO producto (nombre, descripcion, precio, disponibilidad, stock, imagen_url, nombre_categoria) VALUES
    ('Café Espresso Doble', 'Extracción intensa de granos de origen Huila', 6500.0, true, 80, 'https://placehold.co/400x300/432818/F4F0EC?text=Espresso', 'Bebidas Calientes'),
    ('Cappuccino Tradicional', 'Espresso, leche vaporizada y abundante espuma', 9500.0, true, 60, 'https://placehold.co/400x300/7F5539/F4F0EC?text=Cappuccino', 'Bebidas Calientes'),
    ('Caramel Cold Brew', 'Café infusionado en frío por 18 horas con sirope de caramelo', 12500.0, true, 40, 'https://placehold.co/400x300/9C6644/F4F0EC?text=Cold+Brew', 'Bebidas Frías'),
    ('Croissant de Almendras', 'Hojaldre relleno de crema frangipane y almendras tostadas', 8500.0, true, 25, 'https://placehold.co/400x300/B08968/FFFFFF?text=Croissant', 'Repostería y Panadería'),
    ('Sandwich Pavo & Queso Brie', 'Pan masa madre, pechuga de pavo ahumada y reducción de frutos rojos', 22000.0, true, 18, 'https://placehold.co/400x300/EDE0D4/432818?text=Sandwich+Pavo', 'Almuerzos Ligeros');
