-- =========================================================
-- 02_insercion_registros.sql
-- Ejecutar una sola vez después del archivo 01.
-- Datos ficticios. Importes en COP.
-- =========================================================

BEGIN;

-- =========================================================
-- 1. CLIENTE: 50
-- =========================================================

INSERT INTO cliente (id_cliente, nombre, apellido)
SELECT
    n,
    (ARRAY[
        'Ana', 'Juan', 'Laura', 'Carlos', 'Sofia',
        'Andres', 'Valentina', 'Camilo', 'Daniela', 'Mateo'
    ])[((n - 1) % 10) + 1],
    'Prueba ' || n
FROM generate_series(1, 50) AS g(n);

-- =========================================================
-- 2. CLIENTE_TELEFONO: 60
-- =========================================================

INSERT INTO cliente_telefono (telefono, cliente_id_cliente)
SELECT
    (3000000000::BIGINT + n)::VARCHAR,
    n
FROM generate_series(1, 50) AS g(n);

INSERT INTO cliente_telefono (telefono, cliente_id_cliente)
SELECT
    (3100000000::BIGINT + n)::VARCHAR,
    n
FROM generate_series(1, 10) AS g(n);

-- =========================================================
-- 3. CLIENTE_CORREO: 60
-- =========================================================

INSERT INTO cliente_correo (correo, cliente_id_cliente)
SELECT
    'cliente' || n || '@example.com',
    n
FROM generate_series(1, 50) AS g(n);

INSERT INTO cliente_correo (correo, cliente_id_cliente)
SELECT
    'cliente' || n || '.alterno@example.com',
    n
FROM generate_series(1, 10) AS g(n);

-- =========================================================
-- 4. STAFF: 50
-- Contraseña de prueba almacenada mediante hash bcrypt.
-- =========================================================

INSERT INTO staff (
    id_usuario,
    nombres,
    apellidos,
    correo,
    contrasena,
    rol,
    dos_fac_hab,
    fecha_inicio
)
SELECT
    n,
    'Usuario ' || n,
    'Savana',
    'staff' || n || '@example.com',
    crypt('DemoSavana2026!', gen_salt('bf', 10)),
    CASE
        WHEN n <= 5 THEN 'ADMINISTRADOR'
        ELSE 'MESERO'
    END,
    FALSE,
    DATE '2026-09-01' + ((n - 1) % 20)
FROM generate_series(1, 50) AS g(n);

-- =========================================================
-- 5. ADMINISTRADOR: 5
-- =========================================================

INSERT INTO administrador (id_usuario)
SELECT n
FROM generate_series(1, 5) AS g(n);

-- =========================================================
-- 6. MESERO: 45
-- =========================================================

INSERT INTO mesero (id_usuario, estado)
SELECT n, 'ACTIVO'
FROM generate_series(6, 50) AS g(n);

-- =========================================================
-- 7. MESA: 12
-- =========================================================

INSERT INTO mesa (num_mesa, capacidad, estado)
SELECT
    n,
    CASE
        WHEN n % 4 = 1 THEN 2
        WHEN n % 4 = 2 THEN 4
        WHEN n % 4 = 3 THEN 6
        ELSE 8
    END,
    'DISPONIBLE'
FROM generate_series(1, 12) AS g(n);

-- =========================================================
-- 8. CATEGORIA: 5
-- =========================================================

INSERT INTO categoria (
    nombre_categoria,
    descripcion,
    estado
)
VALUES
    ('Entradas', 'Platos para comenzar', 'ACTIVA'),
    ('Platos fuertes', 'Platos principales', 'ACTIVA'),
    ('Bebidas calientes', 'Cafe y otras bebidas calientes', 'ACTIVA'),
    ('Bebidas frias', 'Jugos y otras bebidas frias', 'ACTIVA'),
    ('Postres', 'Opciones dulces', 'ACTIVA');

-- =========================================================
-- 9. PRODUCTO: 20
-- Se completan todos los atributos.
-- Las rutas de imagen son ejemplos.
-- =========================================================

INSERT INTO producto (
    id_producto,
    nombre,
    descripcion,
    precio,
    disponibilidad,
    categoria_nombre_categoria,
    imagen_url,
    stock
)
SELECT
    v.id,
    v.nombre,
    'Porcion de ' || v.nombre,
    v.precio,
    TRUE,
    v.categoria,
    '/imagenes/producto-' || v.id || '.jpg',
    100
FROM (
    VALUES
        (1, 'Empanadas', 9000, 'Entradas'),
        (2, 'Patacones', 12000, 'Entradas'),
        (3, 'Bruschettas', 14000, 'Entradas'),
        (4, 'Sopa del dia', 11000, 'Entradas'),
        (5, 'Pollo a la plancha', 28000, 'Platos fuertes'),
        (6, 'Pasta con verduras', 26000, 'Platos fuertes'),
        (7, 'Hamburguesa Savana', 30000, 'Platos fuertes'),
        (8, 'Arroz con pollo', 25000, 'Platos fuertes'),
        (9, 'Cafe americano', 6000, 'Bebidas calientes'),
        (10, 'Cappuccino', 8500, 'Bebidas calientes'),
        (11, 'Chocolate caliente', 8000, 'Bebidas calientes'),
        (12, 'Aromatica', 5000, 'Bebidas calientes'),
        (13, 'Limonada natural', 7000, 'Bebidas frias'),
        (14, 'Jugo de mora', 7500, 'Bebidas frias'),
        (15, 'Agua', 4000, 'Bebidas frias'),
        (16, 'Soda de frutos rojos', 9000, 'Bebidas frias'),
        (17, 'Torta de chocolate', 12000, 'Postres'),
        (18, 'Cheesecake', 14000, 'Postres'),
        (19, 'Helado', 8000, 'Postres'),
        (20, 'Flan', 10000, 'Postres')
) AS v(id, nombre, precio, categoria);

-- =========================================================
-- 10. METODO_PAGO: 3
-- =========================================================

INSERT INTO metodo_pago (tipo)
VALUES
    ('EFECTIVO'),
    ('TARJETA'),
    ('TRANSFERENCIA');

-- =========================================================
-- 11. RESERVA: 50
-- Cada mesa recibe como máximo una reserva por día.
-- Todas tienen dos personas, sin exceder capacidad.
-- =========================================================

INSERT INTO reserva (
    id_reserva,
    fecha,
    hora,
    num_personas,
    estado,
    mesa_num_mesa,
    cliente_id_cliente
)
SELECT
    n,
    DATE '2026-10-01' + ((n - 1) / 12),
    TIME '12:00',
    2,
    CASE
        WHEN n % 10 = 0 THEN 'CANCELADA'
        ELSE 'COMPLETADA'
    END,
    ((n - 1) % 12) + 1,
    n
FROM generate_series(1, 50) AS g(n);

-- =========================================================
-- 12. PEDIDO: 60
-- Exclusivamente meseros (IDs 6 a 50) registran pedidos.
-- Todos tienen cliente y mesa.
-- =========================================================

INSERT INTO pedido (
    id_pedido,
    fecha,
    estado,
    observaciones,
    staff_id_usuario,
    mesa_num_mesa,
    cliente_id_cliente
)
SELECT
    n,
    TIMESTAMP '2026-10-01 12:15:00'
        + ((n - 1) / 12) * INTERVAL '1 day',
    CASE
        WHEN n <= 50 THEN 'PAGADO'
        WHEN n <= 55 THEN 'PENDIENTE'
        ELSE 'EN_PREPARACION'
    END,
    'Pedido de demostracion ' || n,
    6 + ((n - 1) % 45),
    ((n - 1) % 12) + 1,
    ((n - 1) % 50) + 1
FROM generate_series(1, 60) AS g(n);

-- =========================================================
-- 13. DETALLE_PEDIDO: 120
-- Dos productos diferentes por pedido.
-- Conserva el precio de la venta.
-- =========================================================

INSERT INTO detalle_pedido (
    cantidad,
    precio,
    pedido_id_pedido,
    producto_id_producto
)
SELECT
    1,
    pr.precio,
    pe.id_pedido,
    pr.id_producto
FROM pedido pe
JOIN producto pr
    ON pr.id_producto = ((pe.id_pedido - 1) % 8) + 1;

INSERT INTO detalle_pedido (
    cantidad,
    precio,
    pedido_id_pedido,
    producto_id_producto
)
SELECT
    1,
    pr.precio,
    pe.id_pedido,
    pr.id_producto
FROM pedido pe
JOIN producto pr
    ON pr.id_producto = ((pe.id_pedido - 1) % 8) + 9;

-- =========================================================
-- 14. FACTURA: 50
-- Impuesto IVA del 19 % (0.19).
-- Propina de ejemplo: cero.
-- =========================================================

INSERT INTO factura (
    num_factura,
    fecha,
    estado,
    total,
    pedido_id_pedido,
    impuesto,
    propina
)
SELECT
    'FAC-2026-' || LPAD(p.id_pedido::text, 5, '0'),
    p.fecha + INTERVAL '45 minutes',
    'PAGADA',
    v.subtotal + ROUND(v.subtotal * 0.19, 2),
    p.id_pedido,
    ROUND(v.subtotal * 0.19, 2),
    0
FROM pedido p
JOIN vw_subtotal_pedido v
    ON v.id_pedido = p.id_pedido
WHERE p.estado = 'PAGADO';

-- =========================================================
-- 15. PAGO: 75
-- Las primeras 25 facturas tienen dos pagos.
-- Las siguientes 25 tienen uno.
-- =========================================================

INSERT INTO pago (
    id_pago,
    monto,
    fecha,
    estado,
    factura_num_factura,
    metodo_pago_tipo
)
SELECT
    pedido_id_pedido * 2 - 1,
    ROUND(total / 2, 2),
    fecha + INTERVAL '5 minutes',
    'REGISTRADO',
    'FAC-2026-' || LPAD(pedido_id_pedido::text, 5, '0'),
    'EFECTIVO'
FROM factura
WHERE pedido_id_pedido <= 25;

INSERT INTO pago (
    id_pago,
    monto,
    fecha,
    estado,
    factura_num_factura,
    metodo_pago_tipo
)
SELECT
    pedido_id_pedido * 2,
    total - ROUND(total / 2, 2),
    fecha + INTERVAL '5 minutes',
    'REGISTRADO',
    'FAC-2026-' || LPAD(pedido_id_pedido::text, 5, '0'),
    'TARJETA'
FROM factura
WHERE pedido_id_pedido <= 25;

INSERT INTO pago (
    id_pago,
    monto,
    fecha,
    estado,
    factura_num_factura,
    metodo_pago_tipo
)
SELECT
    pedido_id_pedido + 25,
    total,
    fecha + INTERVAL '5 minutes',
    'REGISTRADO',
    'FAC-2026-' || LPAD(pedido_id_pedido::text, 5, '0'),
    'TRANSFERENCIA'
FROM factura
WHERE pedido_id_pedido > 25;

-- =========================================================
-- AJUSTE DE LAS SECUENCIAS DE IDENTIDAD
-- =========================================================

SELECT setval(
    pg_get_serial_sequence('cliente', 'id_cliente'),
    (SELECT MAX(id_cliente) FROM cliente),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('staff', 'id_usuario'),
    (SELECT MAX(id_usuario) FROM staff),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('producto', 'id_producto'),
    (SELECT MAX(id_producto) FROM producto),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('reserva', 'id_reserva'),
    (SELECT MAX(id_reserva) FROM reserva),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('pedido', 'id_pedido'),
    (SELECT MAX(id_pedido) FROM pedido),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('pago', 'id_pago'),
    (SELECT MAX(id_pago) FROM pago),
    TRUE
);

COMMIT;