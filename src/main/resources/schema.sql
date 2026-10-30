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
DROP TABLE IF EXISTS cliente_correo CASCADE;
DROP TABLE IF EXISTS cliente_telefono CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS metodo_pago CASCADE;

-- 1. TABLA: cliente y tablas hijas de contacto (cliente_correo y cliente_telefono)
CREATE TABLE cliente (
    id_cliente BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL DEFAULT ''
);

CREATE TABLE cliente_telefono (
    telefono VARCHAR(20) NOT NULL,
    cliente_id_cliente BIGINT NOT NULL,
    PRIMARY KEY (telefono, cliente_id_cliente),
    CONSTRAINT fk_cliente_telefono FOREIGN KEY (cliente_id_cliente) REFERENCES cliente(id_cliente) ON DELETE CASCADE
);

CREATE TABLE cliente_correo (
    correo VARCHAR(250) PRIMARY KEY,
    cliente_id_cliente BIGINT NOT NULL,
    CONSTRAINT fk_cliente_correo FOREIGN KEY (cliente_id_cliente) REFERENCES cliente(id_cliente) ON DELETE CASCADE
);

-- 2. TABLA PADRE: staff (Jerarquía de personal sin columnas OTP en base de datos)
CREATE TABLE staff (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL DEFAULT '',
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'MESERO',
    dos_fac_hab BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    CHECK (rol IN ('ADMINISTRADOR', 'MESERO'))
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

-- 5. TABLA: mesa (Salón y capacidad, PK por num_mesa)
CREATE TABLE mesa (
    num_mesa INT PRIMARY KEY,
    capacidad INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE' -- DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO
);

-- 6. TABLA: categoria (Catálogo de productos con nombre_categoria como PK y descripcion NOT NULL)
CREATE TABLE categoria (
    nombre_categoria VARCHAR(100) PRIMARY KEY,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, INACTIVA
    descripcion VARCHAR(255) NOT NULL
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
    categoria_nombre_categoria VARCHAR(100) NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_nombre_categoria) REFERENCES categoria(nombre_categoria) ON UPDATE CASCADE
);

-- 8. TABLA: reserva (Gestión de citas y mesas con fecha y hora, sin observaciones)
CREATE TABLE reserva (
    id_reserva BIGSERIAL PRIMARY KEY,
    cliente_id_cliente BIGINT NOT NULL,
    mesa_num_mesa INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    num_personas INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_reserva_mesa FOREIGN KEY (mesa_num_mesa) REFERENCES mesa(num_mesa)
);

-- 9. TABLA: pedido (Comandas 100% en salón asociadas a mesa, cliente y staff; sin subtotal persistido)
CREATE TABLE pedido (
    id_pedido BIGSERIAL PRIMARY KEY,
    cliente_id_cliente BIGINT NOT NULL,
    mesa_num_mesa INT NOT NULL,
    staff_id_usuario BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_PREPARACION, ENTREGADO, PAGADO, CANCELADO
    observaciones TEXT,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_pedido_mesa FOREIGN KEY (mesa_num_mesa) REFERENCES mesa(num_mesa),
    CONSTRAINT fk_pedido_staff FOREIGN KEY (staff_id_usuario) REFERENCES staff(id_usuario)
);

-- 10. TABLA: detalle_pedido (Relación N:M contener entre Pedido y Producto con clave compuesta y precio unitario)
CREATE TABLE detalle_pedido (
    pedido_id_pedido BIGINT NOT NULL,
    producto_id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    PRIMARY KEY (pedido_id_pedido, producto_id_producto),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id_producto) REFERENCES producto(id_producto)
);

-- 11. TABLA: metodo_pago (Medios de pago soportados con atributo único tipo como PK)
CREATE TABLE metodo_pago (
    tipo VARCHAR(50) PRIMARY KEY -- EFECTIVO, TARJETA, TRANSFERENCIA
);

-- 12. TABLA: factura (Comprobante fiscal 1:1 con pedido, IVA 19%, sin subtotal, id_cliente ni id_usuario)
CREATE TABLE factura (
    num_factura VARCHAR(50) PRIMARY KEY,
    pedido_id_pedido BIGINT NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PAGADA', -- EMITIDA, PAGADA, ANULADA
    propina DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    impuesto DOUBLE PRECISION NOT NULL DEFAULT 0.0, -- 19% IVA
    total DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_factura_pedido FOREIGN KEY (pedido_id_pedido) REFERENCES pedido(id_pedido)
);

-- 13. TABLA: pago (Pagos asociados a una factura y a un metodo_pago con monto y sin referencia)
CREATE TABLE pago (
    id_pago BIGSERIAL PRIMARY KEY,
    factura_num_factura VARCHAR(50) NOT NULL,
    metodo_pago_tipo VARCHAR(50) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PAGADO', -- PAGADO, REGISTRADO, ANULADO
    monto DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_pago_factura FOREIGN KEY (factura_num_factura) REFERENCES factura(num_factura) ON DELETE CASCADE,
    CONSTRAINT fk_pago_metodo FOREIGN KEY (metodo_pago_tipo) REFERENCES metodo_pago(tipo),
    CHECK (estado IN ('PAGADO', 'REGISTRADO', 'ANULADO'))
);

-- ====================================================================
-- DATOS INICIALES (SEMILLA) PARA PRUEBAS INMEDIATAS
-- Contraseña encriptada para todos los miembros de staff: Password123
-- ====================================================================

-- 1. Staff y Herencia (Administrador y Meseros)
INSERT INTO staff (nombres, apellidos, correo, contrasena, rol, dos_fac_hab, fecha_inicio) VALUES
    ('Administrador', 'General', 'admin@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', 'ADMINISTRADOR', false, CURRENT_DATE),
    ('Carlos', 'Mesero', 'mesero@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', 'MESERO', false, CURRENT_DATE),
    ('Luisa', 'Operativa', 'luisa@savanacoffee.com', '$2a$10$rTNB5VmVVrmzQtG9/PJUgurx.wi1/VQwvRCsJGT15oe.J/qpSHN/S', 'MESERO', false, CURRENT_DATE);

INSERT INTO administrador (id_usuario) VALUES (1);

INSERT INTO mesero (id_usuario, estado) VALUES
    (2, 'ACTIVO'),
    (3, 'ACTIVO');

-- 2. Clientes de Ejemplo (Persistencia Permanente con tablas cliente_correo y cliente_telefono)
INSERT INTO cliente (nombre, apellido) VALUES
    ('Mariana', 'Ruiz'),
    ('Juan', 'Perez');

INSERT INTO cliente_correo (correo, cliente_id_cliente) VALUES
    ('mariana.ruiz@gmail.com', 1),
    ('juan.perez@hotmail.com', 2);

INSERT INTO cliente_telefono (telefono, cliente_id_cliente) VALUES
    ('3109876543', 1),
    ('3151234567', 2);

-- 3. Mesas del Salón
INSERT INTO mesa (num_mesa, capacidad, estado) VALUES
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

-- 6. Productos (FK categoria_nombre_categoria)
INSERT INTO producto (nombre, descripcion, precio, disponibilidad, stock, imagen_url, categoria_nombre_categoria) VALUES
    ('Café Espresso Doble', 'Extracción intensa de granos de origen Huila', 6500.0, true, 80, 'https://placehold.co/400x300/432818/F4F0EC?text=Espresso', 'Bebidas Calientes'),
    ('Cappuccino Tradicional', 'Espresso, leche vaporizada y abundante espuma', 9500.0, true, 60, 'https://placehold.co/400x300/7F5539/F4F0EC?text=Cappuccino', 'Bebidas Calientes'),
    ('Caramel Cold Brew', 'Café infusionado en frío por 18 horas con sirope de caramelo', 12500.0, true, 40, 'https://placehold.co/400x300/9C6644/F4F0EC?text=Cold+Brew', 'Bebidas Frías'),
    ('Croissant de Almendras', 'Hojaldre relleno de crema frangipane y almendras tostadas', 8500.0, true, 25, 'https://placehold.co/400x300/B08968/FFFFFF?text=Croissant', 'Repostería y Panadería'),
    ('Sandwich Pavo & Queso Brie', 'Pan masa madre, pechuga de pavo ahumada y reducción de frutos rojos', 22000.0, true, 18, 'https://placehold.co/400x300/EDE0D4/432818?text=Sandwich+Pavo', 'Almuerzos Ligeros');
