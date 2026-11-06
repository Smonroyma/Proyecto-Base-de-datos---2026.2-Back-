BEGIN;

SELECT current_database() AS base_actual;

DROP VIEW IF EXISTS vw_saldo_factura;
DROP VIEW IF EXISTS vw_subtotal_pedido;

DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS factura;
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS reserva;

DROP FUNCTION IF EXISTS validar_capacidad_reserva();

DROP TABLE IF EXISTS administrador;
DROP TABLE IF EXISTS mesero;

DROP TABLE IF EXISTS cliente_telefono;
DROP TABLE IF EXISTS cliente_correo;

DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS metodo_pago;
DROP TABLE IF EXISTS mesa;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS cliente;

ROLLBACK;
