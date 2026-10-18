-- ====================================================================
-- SAVANA DATA COFFEE - SCRIPT DE INICIALIZACIÓN H2 (MODO POSTGRESQL)
-- ====================================================================

DROP TABLE IF EXISTS pago_factura CASCADE;
DROP TABLE IF EXISTS factura CASCADE;
DROP TABLE IF EXISTS detalle_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS reserva CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS mesa CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS metodo_pago CASCADE;

-- 1. TABLA: usuario (Personal administrativo y operativo)
CREATE TABLE usuario (
                         id_usuario BIGSERIAL PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         password_hash VARCHAR(255) NOT NULL,
                         rol VARCHAR(30) NOT NULL,
                         activo BOOLEAN NOT NULL DEFAULT TRUE,
                         dos_factores_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
                         codigo_otp VARCHAR(10),
                         expiracion_otp TIMESTAMP,
                         fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA: cliente (Comensales y clientes frecuentes)
CREATE TABLE cliente (
                         id_cliente BIGSERIAL PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         telefono VARCHAR(20) NOT NULL UNIQUE,
                         email VARCHAR(100),
                         identificacion VARCHAR(30),
                         fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA: mesa (Salón y capacidad)
CREATE TABLE mesa (
                      id_mesa BIGSERIAL PRIMARY KEY,
                      numero INT NOT NULL UNIQUE,
                      capacidad INT NOT NULL,
                      estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE' -- DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO
);

-- 4. TABLA: categoria (Catálogo de productos)
CREATE TABLE categoria (
                           id_categoria BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL UNIQUE,
                           descripcion TEXT,
                           activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. TABLA: producto (Bebidas, comidas y repostería)
CREATE TABLE producto (
                          id_producto BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL UNIQUE,
                          descripcion TEXT,
                          precio DOUBLE PRECISION NOT NULL,
                          id_categoria BIGINT NOT NULL,
                          imagen_url VARCHAR(255),
                          disponible BOOLEAN NOT NULL DEFAULT TRUE,
                          stock INT NOT NULL DEFAULT 0,
                          CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

-- 6. TABLA: reserva (Gestión de citas y mesas)
CREATE TABLE reserva (
                         id_reserva BIGSERIAL PRIMARY KEY,
                         id_cliente BIGINT NOT NULL,
                         id_mesa BIGINT NOT NULL,
                         fecha_hora TIMESTAMP NOT NULL,
                         numero_personas INT NOT NULL,
                         estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
                         observaciones TEXT,
                         CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
                         CONSTRAINT fk_reserva_mesa FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
);

-- 7. TABLA: pedido (Comandas de salón y para llevar)
CREATE TABLE pedido (
                        id_pedido BIGSERIAL PRIMARY KEY,
                        id_cliente BIGINT NOT NULL,
                        id_mesa BIGINT, -- Nullable si es para llevar
                        id_usuario BIGINT NOT NULL,
                        fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_PREPARACION, ENTREGADO, CANCELADO, PAGADO
                        tipo_pedido VARCHAR(20) NOT NULL DEFAULT 'MESA', -- MESA, LLEVAR
                        total DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                        observaciones TEXT,
                        CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
                        CONSTRAINT fk_pedido_mesa FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa),
                        CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- 8. TABLA: detalle_pedido (Ítems y precios congelados)
CREATE TABLE detalle_pedido (
                                id_detalle_pedido BIGSERIAL PRIMARY KEY,
                                id_pedido BIGINT NOT NULL,
                                id_producto BIGINT NOT NULL,
                                cantidad INT NOT NULL,
                                precio_unitario DOUBLE PRECISION NOT NULL,
                                subtotal DOUBLE PRECISION NOT NULL,
                                CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE,
                                CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- 9. TABLA: metodo_pago (Medios de pago soportados)
CREATE TABLE metodo_pago (
                             id_metodo_pago BIGSERIAL PRIMARY KEY,
                             codigo VARCHAR(50) NOT NULL UNIQUE,
                             nombre VARCHAR(100) NOT NULL,
                             descripcion TEXT,
                             activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 10. TABLA: factura (Comprobante fiscal y liquidación)
CREATE TABLE factura (
                         id_factura BIGSERIAL PRIMARY KEY,
                         numero_factura VARCHAR(50) NOT NULL UNIQUE,
                         id_pedido BIGINT NOT NULL UNIQUE,
                         id_cliente BIGINT NOT NULL,
                         id_usuario BIGINT NOT NULL,
                         fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         subtotal DOUBLE PRECISION NOT NULL,
                         propina DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         descuento DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         impuesto DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         total DOUBLE PRECISION NOT NULL,
                         estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDA', -- EMITIDA, ANULADA, PAGADA
                         CONSTRAINT fk_factura_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
                         CONSTRAINT fk_factura_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
                         CONSTRAINT fk_factura_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- 11. TABLA: pago_factura (Soporte de pagos múltiples por factura)
CREATE TABLE pago_factura (
                              id_pago BIGSERIAL PRIMARY KEY,
                              id_factura BIGINT NOT NULL,
                              id_metodo_pago BIGINT NOT NULL,
                              monto DOUBLE PRECISION NOT NULL,
                              referencia_transaccion VARCHAR(100),
                              fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_pago_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE CASCADE,
                              CONSTRAINT fk_pago_metodo FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id_metodo_pago)
);

-- ====================================================================
-- DATOS INICIALES (SEMILLA) PARA PRUEBAS INMEDIATAS
-- Contraseña encriptada para todos los usuarios: Password123
-- ====================================================================

-- 1. Usuarios Staff (Password: Password123)
INSERT INTO usuario (nombre, email, password_hash, rol, activo, dos_factores_habilitado) VALUES
                                                                                             ('Administrador General', 'admin@savanacoffee.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMINISTRADOR', true, false),
                                                                                             ('Carlos Barista', 'barista@savanacoffee.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'BARISTA', true, false),
                                                                                             ('Luisa Cajera', 'cajero@savanacoffee.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CAJERO', true, false);

-- 2. Clientes de Ejemplo
INSERT INTO cliente (nombre, telefono, email, identificacion) VALUES
                                                                  ('Mariana Ruiz', '3109876543', 'mariana.ruiz@gmail.com', '1020304050'),
                                                                  ('Juan Perez', '3151234567', 'juan.perez@hotmail.com', '80123456');

-- 3. Mesas del Salón
INSERT INTO mesa (numero, capacidad, estado) VALUES
                                                 (1, 2, 'DISPONIBLE'),
                                                 (2, 4, 'DISPONIBLE'),
                                                 (3, 4, 'DISPONIBLE'),
                                                 (4, 6, 'DISPONIBLE'),
                                                 (5, 8, 'DISPONIBLE');

-- 4. Métodos de Pago
INSERT INTO metodo_pago (codigo, nombre, descripcion, activo) VALUES
                                                                  ('EFECTIVO', 'Efectivo', 'Pago directo en caja física', true),
                                                                  ('TARJETA_DEBITO', 'Tarjeta Débito', 'Datáfono / Redeban', true),
                                                                  ('TARJETA_CREDITO', 'Tarjeta Crédito', 'Datáfono / Visa / Mastercard', true),
                                                                  ('TRANSFERENCIA_QR', 'Transferencia QR', 'Nequi / Daviplata / Bre-B', true);

-- 5. Categorías del Menú
INSERT INTO categoria (nombre, descripcion, activo) VALUES
                                                        ('Bebidas Calientes', 'Cafés de especialidad, espressos y lattes', true),
                                                        ('Bebidas Frías', 'Frappés, cold brew y sodas saborizadas', true),
                                                        ('Repostería y Panadería', 'Pastelería fresca horneada a diario', true),
                                                        ('Almuerzos Ligeros', 'Sandwiches artesanales y ensaladas', true);

-- 6. Productos
INSERT INTO producto (nombre, descripcion, precio, id_categoria, imagen_url, disponible, stock) VALUES
                                                                                                    ('Café Espresso Doble', 'Extracción intensa de granos de origen Huila', 6500.0, 1, 'https://placehold.co/400x300/432818/F4F0EC?text=Espresso', true, 80),
                                                                                                    ('Cappuccino Tradicional', 'Espresso, leche vaporizada y abundante espuma', 9500.0, 1, 'https://placehold.co/400x300/7F5539/F4F0EC?text=Cappuccino', true, 60),
                                                                                                    ('Caramel Cold Brew', 'Café infusionado en frío por 18 horas con sirope de caramelo', 12500.0, 2, 'https://placehold.co/400x300/9C6644/F4F0EC?text=Cold+Brew', true, 40),
                                                                                                    ('Croissant de Almendras', 'Hojaldre relleno de crema frangipane y almendras tostadas', 8500.0, 3, 'https://placehold.co/400x300/B08968/FFFFFF?text=Croissant', true, 25),
                                                                                                    ('Sandwich Pavo & Queso Brie', 'Pan masa madre, pechuga de pavo ahumada y reducción de frutos rojos', 22000.0, 4, 'https://placehold.co/400x300/EDE0D4/432818?text=Sandwich+Pavo', true, 18);