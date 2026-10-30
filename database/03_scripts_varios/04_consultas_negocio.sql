-- =========================================================
-- 04_consultas_negocio.sql
-- Reportes del restaurante.
-- =========================================================

BEGIN TRANSACTION READ ONLY;

-- 1. Menú disponible.
SELECT
    p.id_producto,
    p.nombre,
    p.categoria_nombre_categoria AS categoria,
    p.precio,
    p.stock,
    p.imagen_url
FROM producto p
JOIN categoria c
    ON c.nombre_categoria = p.categoria_nombre_categoria
WHERE p.disponibilidad = TRUE
  AND p.stock > 0
  AND c.estado = 'ACTIVA'
ORDER BY categoria, p.nombre;

-- 2. Productos más vendidos en pedidos atendidos.
SELECT
    pr.nombre,
    SUM(d.cantidad) AS unidades,
    SUM(d.cantidad * d.precio) AS valor_vendido
FROM detalle_pedido d
JOIN producto pr ON pr.id_producto = d.producto_id_producto
JOIN pedido p ON p.id_pedido = d.pedido_id_pedido
WHERE p.estado IN ('ENTREGADO', 'PAGADO')
GROUP BY pr.id_producto, pr.nombre
ORDER BY unidades DESC, pr.nombre;

-- 3. Ventas facturadas por día, excluyendo anuladas.
SELECT
    fecha::DATE AS dia,
    COUNT(*) AS facturas,
    SUM(total) AS total_facturado
FROM factura
WHERE estado <> 'ANULADA'
GROUP BY fecha::DATE
ORDER BY dia;

-- 4. Recaudo válido por método.
SELECT
    mp.tipo,
    COUNT(pa.id_pago) AS pagos,
    COALESCE(SUM(pa.monto), 0) AS recaudo
FROM metodo_pago mp
LEFT JOIN (
    SELECT p.*
    FROM pago p
    JOIN factura f ON f.num_factura = p.factura_num_factura
    WHERE p.estado IN ('PAGADO', 'REGISTRADO')
      AND f.estado <> 'ANULADA'
) pa ON pa.metodo_pago_tipo = mp.tipo
GROUP BY mp.tipo
ORDER BY recaudo DESC;

-- 5. Facturas con saldo pendiente.
SELECT *
FROM vw_saldo_factura
WHERE estado <> 'ANULADA'
  AND saldo > 0
ORDER BY saldo DESC;

-- 6. Pedidos pendientes y en preparación.
SELECT
    p.id_pedido,
    p.fecha,
    p.estado,
    p.mesa_num_mesa,
    c.nombre AS cliente,
    s.nombres AS responsable,
    p.observaciones
FROM pedido p
JOIN cliente c ON c.id_cliente = p.cliente_id_cliente
JOIN staff s ON s.id_usuario = p.staff_id_usuario
WHERE p.estado IN ('PENDIENTE', 'EN_PREPARACION')
ORDER BY p.fecha, p.id_pedido;

-- 7. Pedidos por responsable.
SELECT
    s.id_usuario,
    s.nombres,
    s.rol,
    COUNT(p.id_pedido) AS pedidos
FROM staff s
LEFT JOIN pedido p ON p.staff_id_usuario = s.id_usuario
GROUP BY s.id_usuario, s.nombres, s.rol
ORDER BY pedidos DESC, s.id_usuario;

-- 8. Ventas por mesa sin duplicar facturas por pagos.
SELECT
    m.num_mesa,
    COUNT(f.num_factura) AS facturas,
    COALESCE(SUM(f.total), 0) AS total_facturado
FROM mesa m
LEFT JOIN pedido p ON p.mesa_num_mesa = m.num_mesa
LEFT JOIN factura f
    ON f.pedido_id_pedido = p.id_pedido
   AND f.estado <> 'ANULADA'
GROUP BY m.num_mesa
ORDER BY total_facturado DESC;

-- 9. Reservas con hora de finalización calculada.
SELECT
    r.id_reserva,
    c.nombre,
    c.apellido,
    r.mesa_num_mesa,
    r.fecha + r.hora AS inicio,
    r.fecha + r.hora + INTERVAL '3 hours' AS fin,
    r.num_personas,
    r.estado
FROM reserva r
JOIN cliente c ON c.id_cliente = r.cliente_id_cliente
ORDER BY inicio, r.id_reserva;

-- 10. Mesas disponibles para una solicitud de ejemplo.
-- Cambiar inicio y personas.
WITH solicitud AS (
    SELECT
        TIMESTAMP '2026-11-15 12:00:00' AS inicio,
        4 AS personas
)
SELECT m.num_mesa, m.capacidad
FROM mesa m
CROSS JOIN solicitud s
WHERE m.estado <> 'EN_MANTENIMIENTO'
  AND m.capacidad >= s.personas
  AND NOT EXISTS (
      SELECT 1
      FROM reserva r
      WHERE r.mesa_num_mesa = m.num_mesa
        AND r.estado <> 'CANCELADA'
        AND r.fecha + r.hora < s.inicio + INTERVAL '3 hours'
        AND r.fecha + r.hora + INTERVAL '3 hours' > s.inicio
  )
ORDER BY m.capacidad, m.num_mesa;

-- 11. Productos con pocas unidades.
SELECT id_producto, nombre, stock
FROM producto
WHERE stock < 10
ORDER BY stock, nombre;

-- 12. Clientes con más reservas no canceladas.
SELECT
    c.id_cliente,
    c.nombre,
    c.apellido,
    COUNT(r.id_reserva) AS reservas
FROM cliente c
LEFT JOIN reserva r
    ON r.cliente_id_cliente = c.id_cliente
   AND r.estado <> 'CANCELADA'
GROUP BY c.id_cliente, c.nombre, c.apellido
ORDER BY reservas DESC, c.id_cliente;

-- 13. Estadísticas de facturación.
SELECT
    ROUND(AVG(total), 2) AS promedio,
    MIN(total) AS minimo,
    MAX(total) AS maximo,
    SUM(total) AS total_facturado
FROM factura
WHERE estado <> 'ANULADA';

COMMIT;