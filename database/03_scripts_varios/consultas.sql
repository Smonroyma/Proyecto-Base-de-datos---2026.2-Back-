SELECT
    c.id_cliente,
    c.nombre,
    c.apellido,
    (
        SELECT STRING_AGG(t.telefono, ', ' ORDER BY t.telefono)
        FROM cliente_telefono t
        WHERE t.cliente_id_cliente = c.id_cliente
    ) AS telefonos,
    (
        SELECT STRING_AGG(co.correo, ', ' ORDER BY co.correo)
        FROM cliente_correo co
        WHERE co.cliente_id_cliente = c.id_cliente
    ) AS correos
FROM cliente c
ORDER BY c.id_cliente;

SELECT
    d.pedido_id_pedido,
    pr.nombre,
    d.cantidad,
    d.precio,
    d.cantidad * d.precio AS subtotal_linea
FROM detalle_pedido d
JOIN producto pr ON pr.id_producto = d.producto_id_producto
WHERE d.pedido_id_pedido = 1;

SELECT id_producto, nombre, precio
FROM producto
WHERE precio > (SELECT AVG(precio) FROM producto)
ORDER BY precio DESC;

SELECT s.id_usuario, s.nombres, s.rol
FROM staff s
WHERE NOT EXISTS (
    SELECT 1
    FROM pedido p
    WHERE p.staff_id_usuario = s.id_usuario
);

BEGIN;

INSERT INTO cliente_telefono (telefono, cliente_id_cliente)
VALUES ('3209998877', 1);

SELECT *
FROM cliente_telefono
WHERE cliente_id_cliente = 1;

ROLLBACK;

BEGIN;

UPDATE producto
SET precio = precio + 1000
WHERE id_producto = 1;

SELECT id_producto, nombre, precio
FROM producto
WHERE id_producto = 1;

SELECT pedido_id_pedido, precio
FROM detalle_pedido
WHERE producto_id_producto = 1;

ROLLBACK;

BEGIN;

UPDATE producto
SET disponibilidad = FALSE
WHERE id_producto = 20;

SELECT id_producto, nombre, disponibilidad
FROM producto
WHERE id_producto = 20;

ROLLBACK;

BEGIN;

UPDATE reserva
SET estado = 'CANCELADA'
WHERE id_reserva = 1;

SELECT *
FROM reserva
WHERE id_reserva = 1;

ROLLBACK;

BEGIN;

DELETE FROM cliente_telefono
WHERE telefono = '3100000001'
  AND cliente_id_cliente = 1;

SELECT *
FROM cliente_telefono
WHERE cliente_id_cliente = 1;

ROLLBACK;

BEGIN;

INSERT INTO reserva (
    fecha,
    hora,
    num_personas,
    estado,
    mesa_num_mesa,
    cliente_id_cliente
)
VALUES (
    DATE '2027-01-15',
    TIME '12:00',
    2,
    'CONFIRMADA',
    1,
    1
);

SELECT
    id_reserva,
    fecha + hora AS inicio,
    fecha + hora + INTERVAL '3 hours' AS fin
FROM reserva
WHERE fecha = DATE '2027-01-15'
  AND mesa_num_mesa = 1;

ROLLBACK;

SELECT estado, COUNT(*) AS cantidad
FROM pedido
GROUP BY estado
ORDER BY cantidad DESC;

SELECT
    (
        SELECT COALESCE(SUM(total), 0)
        FROM factura
        WHERE estado <> 'ANULADA'
    ) AS total_facturado,
    (
        SELECT COALESCE(SUM(p.monto), 0)
        FROM pago p
        JOIN factura f ON f.num_factura = p.factura_num_factura
        WHERE p.estado IN ('PAGADO', 'REGISTRADO')
          AND f.estado <> 'ANULADA'
    ) AS total_recaudado;

SELECT
    f.num_factura,
    f.pedido_id_pedido,
    f.impuesto,
    f.total,
    p.id_pago,
    p.factura_num_factura,
    p.monto,
    p.estado
FROM factura f
JOIN pago p ON p.factura_num_factura = f.num_factura
WHERE f.num_factura = 'FAC-2026-' || LPAD(1::text, 5, '0');
