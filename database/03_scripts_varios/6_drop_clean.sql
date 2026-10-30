-- =========================================================
-- 06_drop_clean.sql
-- Limpieza del entorno de pruebas.
-- No ejecutar durante la instalación normal.
-- =========================================================

BEGIN;

SELECT current_database() AS base_actual;

-- Vistas.
DROP VIEW IF EXISTS vw_saldo_factura;
DROP VIEW IF EXISTS vw_subtotal_pedido;

-- Tablas dependientes.
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS factura;
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS reserva;

-- El trigger de capacidad desaparece con reserva.
DROP FUNCTION IF EXISTS validar_capacidad_reserva();

-- Subtipos.
DROP TABLE IF EXISTS administrador;
DROP TABLE IF EXISTS mesero;

-- Contactos.
DROP TABLE IF EXISTS cliente_telefono;
DROP TABLE IF EXISTS cliente_correo;

-- Tablas principales y catálogos.
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS metodo_pago;
DROP TABLE IF EXISTS mesa;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS cliente;

-- Índices y secuencias de identidad se eliminan
-- junto con las tablas propietarias.
-- No se eliminan extensiones que podrían compartirse.

ROLLBACK;

-- Para limpieza definitiva, cambiar ROLLBACK por COMMIT.