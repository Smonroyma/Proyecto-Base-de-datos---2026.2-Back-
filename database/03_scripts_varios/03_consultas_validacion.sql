-- =========================================================
-- 03_consultas_validacion.sql
-- Consultas de estructura y calidad de los datos.
-- =========================================================

BEGIN TRANSACTION READ ONLY;

-- 1. Tablas y vistas.
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_type, table_name;

-- 2. Conteos de las 15 tablas.
SELECT 'cliente' AS tabla, COUNT(*) AS registros FROM cliente
UNION ALL SELECT 'cliente_telefono', COUNT(*) FROM cliente_telefono
UNION ALL SELECT 'cliente_correo', COUNT(*) FROM cliente_correo
UNION ALL SELECT 'staff', COUNT(*) FROM staff
UNION ALL SELECT 'administrador', COUNT(*) FROM administrador
UNION ALL SELECT 'mesero', COUNT(*) FROM mesero
UNION ALL SELECT 'mesa', COUNT(*) FROM mesa
UNION ALL SELECT 'categoria', COUNT(*) FROM categoria
UNION ALL SELECT 'producto', COUNT(*) FROM producto
UNION ALL SELECT 'metodo_pago', COUNT(*) FROM metodo_pago
UNION ALL SELECT 'reserva', COUNT(*) FROM reserva
UNION ALL SELECT 'pedido', COUNT(*) FROM pedido
UNION ALL SELECT 'detalle_pedido', COUNT(*) FROM detalle_pedido
UNION ALL SELECT 'factura', COUNT(*) FROM factura
UNION ALL SELECT 'pago', COUNT(*) FROM pago
ORDER BY tabla;

-- 3. Claves y restricciones.
SELECT
    c.relname AS tabla,
    co.conname AS restriccion,
    pg_get_constraintdef(co.oid) AS definicion
FROM pg_constraint co
JOIN pg_class c ON c.oid = co.conrelid
JOIN pg_namespace ns ON ns.oid = c.relnamespace
WHERE ns.nspname = 'public'
ORDER BY c.relname, co.conname;

-- 4. Índices.
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- 5. Catálogos.
SELECT * FROM categoria ORDER BY nombre_categoria;
SELECT * FROM metodo_pago ORDER BY tipo;
SELECT * FROM mesa ORDER BY num_mesa;

-- Las consultas 6 a 18 deben devolver cero filas
-- con los datos de prueba.

-- 6. STAFF sin subtipo.
SELECT s.id_usuario
FROM staff s
LEFT JOIN administrador a ON a.id_usuario = s.id_usuario
LEFT JOIN mesero m ON m.id_usuario = s.id_usuario
WHERE a.id_usuario IS NULL
  AND m.id_usuario IS NULL;

-- 7. STAFF que pertenece a ambos subtipos.
SELECT a.id_usuario
FROM administrador a
JOIN mesero m ON m.id_usuario = a.id_usuario;

-- 8. Rol que no coincide con su subtipo.
SELECT s.id_usuario, s.rol
FROM staff s
LEFT JOIN administrador a ON a.id_usuario = s.id_usuario
LEFT JOIN mesero m ON m.id_usuario = s.id_usuario
WHERE (s.rol = 'ADMINISTRADOR' AND a.id_usuario IS NULL)
   OR (s.rol = 'MESERO' AND m.id_usuario IS NULL);

-- 9. Clientes sin teléfonos.
SELECT c.id_cliente
FROM cliente c
WHERE NOT EXISTS (
    SELECT 1 FROM cliente_telefono t
    WHERE t.cliente_id_cliente = c.id_cliente
);

-- 10. Clientes sin correos.
SELECT c.id_cliente
FROM cliente c
WHERE NOT EXISTS (
    SELECT 1 FROM cliente_correo co
    WHERE co.cliente_id_cliente = c.id_cliente
);

-- 11. Pedidos sin detalle.
SELECT p.id_pedido
FROM pedido p
WHERE NOT EXISTS (
    SELECT 1 FROM detalle_pedido d
    WHERE d.pedido_id_pedido = p.id_pedido
);

-- 12. Pedidos entregados o pagados sin factura.
SELECT p.id_pedido
FROM pedido p
LEFT JOIN factura f ON f.pedido_id_pedido = p.id_pedido
WHERE p.estado IN ('ENTREGADO', 'PAGADO')
  AND f.num_factura IS NULL;

-- 13. Facturas de pedidos no entregados.
SELECT f.num_factura, p.estado
FROM factura f
JOIN pedido p ON p.id_pedido = f.pedido_id_pedido
WHERE p.estado NOT IN ('ENTREGADO', 'PAGADO');

-- 14. Totales de factura diferentes del cálculo.
SELECT f.num_factura, f.total, v.subtotal
FROM factura f
JOIN vw_subtotal_pedido v ON v.id_pedido = f.pedido_id_pedido
WHERE f.total <>
    v.subtotal + f.impuesto + COALESCE(f.propina, 0);

-- 15. Sobrepagos o facturas marcadas pagadas con saldo.
SELECT *
FROM vw_saldo_factura
WHERE saldo < 0
   OR (estado = 'PAGADA' AND saldo <> 0);

-- 16. Reservas superiores a la capacidad.
SELECT r.id_reserva, r.num_personas, m.capacidad
FROM reserva r
JOIN mesa m ON m.num_mesa = r.mesa_num_mesa
WHERE r.estado <> 'CANCELADA'
  AND r.num_personas > m.capacidad;

-- 17. Reservas cruzadas en bloques de tres horas.
SELECT
    r1.id_reserva AS reserva_1,
    r2.id_reserva AS reserva_2,
    r1.mesa_num_mesa
FROM reserva r1
JOIN reserva r2
    ON r1.mesa_num_mesa = r2.mesa_num_mesa
   AND r1.id_reserva < r2.id_reserva
WHERE r1.estado <> 'CANCELADA'
  AND r2.estado <> 'CANCELADA'
  AND r1.fecha + r1.hora <
      r2.fecha + r2.hora + INTERVAL '3 hours'
  AND r2.fecha + r2.hora <
      r1.fecha + r1.hora + INTERVAL '3 hours';

-- 18. Inconsistencias cronológicas.
SELECT 'factura' AS objeto, f.num_factura AS id
FROM factura f
JOIN pedido p ON p.id_pedido = f.pedido_id_pedido
WHERE f.fecha < p.fecha
UNION ALL
SELECT 'pago', pa.id_pago::VARCHAR
FROM pago pa
JOIN factura f ON f.num_factura = pa.factura_num_factura
WHERE pa.fecha < f.fecha;

-- 19. Saldos: 50 filas, todos en cero.
SELECT *
FROM vw_saldo_factura
ORDER BY num_factura;

-- 20. Facturas con varios métodos: 25 filas.
SELECT
    factura_num_factura,
    COUNT(*) AS numero_pagos,
    COUNT(DISTINCT metodo_pago_tipo) AS metodos,
    SUM(monto) AS total_pagado
FROM pago
WHERE estado IN ('PAGADO', 'REGISTRADO')
GROUP BY factura_num_factura
HAVING COUNT(DISTINCT metodo_pago_tipo) > 1
ORDER BY factura_num_factura;

-- 21. Verificación de tipos booleanos nativos en staff y producto.
SELECT COUNT(*) AS staff_sin_2fa FROM staff WHERE dos_fac_hab = FALSE;
SELECT COUNT(*) AS productos_disponibles FROM producto WHERE disponibilidad = TRUE;

COMMIT;