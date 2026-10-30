# Base de datos Savana

Universidad El Bosque  
Programa: Ingeniería de Sistemas  
Asignatura: Bases de Datos 1  
Motor: PostgreSQL 16 o posterior

## Objetivo

Implementar el modelo relacional del restaurante Savana mediante
scripts de creación, inserción, validación, consultas de negocio,
respaldo, restauración y limpieza.

## Requisitos

- PostgreSQL 16 o posterior.
- pgAdmin o psql.
- Permisos para crear objetos y extensiones.
- Extensiones btree_gist y pgcrypto disponibles.
- Herramientas pg_dump, pg_restore y createdb para respaldos.
- Archivos guardados en UTF-8.

## Organización

| Carpeta | Archivo |
|---|---|
| 01_creacion | 01_creacion_objetos.sql |
| 02_insercion | 02_insercion_registros.sql |
| 03_scripts_varios | 03_consultas_validacion.sql |
| 03_scripts_varios | 04_consultas_negocio.sql |
| 03_scripts_varios | 05_backup_restore.sql |
| 03_scripts_varios | 06_drop_clean.sql |
| 03_scripts_varios | consultas.sql |

## Crear la base

Conectado a postgres, ejecutar por separado:

```sql
CREATE DATABASE savana
    ENCODING 'UTF8'
    TEMPLATE template0;
```

Después abrir una nueva conexión a savana.

## Orden de ejecución

1. 01_creacion_objetos.sql
2. 02_insercion_registros.sql
3. 03_consultas_validacion.sql
4. 04_consultas_negocio.sql
5. consultas.sql, cuando se necesiten sus ejemplos.

Los archivos 01 y 02 se ejecutan una sola vez en una base vacía.

05_backup_restore.sql contiene instrucciones comentadas para terminal.
Ejecutarlo en Query Tool no genera un respaldo.

06_drop_clean.sql se utiliza únicamente para limpiar pruebas.
Termina con ROLLBACK; cambiarlo a COMMIT hace permanente la eliminación.

Si una transacción falla, ejecutar ROLLBACK, corregir la causa y
volver a ejecutar el archivo correspondiente.

## Modelo implementado

- CLIENTE y sus teléfonos y correos.
- STAFF con subtipos ADMINISTRADOR y MESERO.
- MESA.
- CATEGORIA y PRODUCTO.
- RESERVA.
- PEDIDO y DETALLE_PEDIDO.
- FACTURA.
- PAGO y METODO_PAGO.

Se normaliza el nombre del identificador de STAFF como id_usuario.

Las claves naturales del modelo se conservan:
num_mesa, nombre_categoria y tipo de método de pago.

## Reglas implementadas en SQL

- Cada pedido requiere cliente, mesa y responsable.
- STAFF puede ser referenciado por pedidos de administradores o meseros.
- Cada reserva requiere cliente y mesa.
- id_reserva es la única PK de RESERVA.
- El correo de cliente no se comparte entre clientes.
- Un pedido admite como máximo una factura.
- Una factura admite varios pagos y métodos.
- Los precios y montos son positivos.
- El stock no es negativo.
- Cada producto de un pedido aparece una vez, con su cantidad.
- Cada reserva no cancelada bloquea tres horas.
- No se permiten cruces de reservas para la misma mesa.
- Al insertar o modificar una reserva se valida capacidad y mantenimiento.

La restricción de exclusión permite reservas consecutivas:
una reserva de 12:00 a 15:00 permite otra desde las 15:00.

El intervalo se calcula con fecha y hora completas, por lo que también
funciona cuando las tres horas atraviesan la medianoche.

La disponibilidad por horario se determina mediante reservas.
El estado actual de una mesa no representa por sí solo su disponibilidad
para todas las fechas futuras.

## Importes y fechas

Los importes usan NUMERIC(12,2), en pesos colombianos.

El subtotal no se almacena en PEDIDO.
La vista vw_subtotal_pedido lo calcula con cantidad por precio.

FACTURA conserva total, impuesto y propina:

total = subtotal calculado + impuesto + propina

Impuesto y propina son importes monetarios.
El 10 % utilizado en la carga es exclusivamente un ejemplo de prueba;
no establece la tributación real del restaurante.

El precio de DETALLE_PEDIDO es el precio histórico de venta.

Las fechas con hora se interpretan como hora local de Bogotá.

## Datos esperados

| Tabla | Filas |
|---|---:|
| cliente | 50 |
| cliente_telefono | 60 |
| cliente_correo | 60 |
| staff | 50 |
| administrador | 5 |
| mesero | 45 |
| mesa | 12 |
| categoria | 5 |
| producto | 20 |
| metodo_pago | 3 |
| reserva | 50 |
| pedido | 60 |
| detalle_pedido | 120 |
| factura | 50 |
| pago | 75 |

Todas las tablas reciben datos.
La carga completa también los atributos opcionales de la muestra.

Hay 50 pedidos entregados con factura, cinco pendientes y cinco en
preparación. Las primeras 25 facturas tienen dos pagos y las demás uno.

Las imágenes se representan con rutas de ejemplo. Los archivos de imagen
deben agregarse al frontend.

## Cuentas de prueba

Los 50 usuarios tienen la contraseña de demostración:

DemoSavana2026!

Se guarda como hash bcrypt mediante pgcrypto.
No utilizar estas credenciales compartidas para cuentas reales.

El backend debe verificar el hash con un mecanismo compatible.
Este script no implementa el inicio de sesión ni el segundo factor.

## Validación

Con los datos iniciales:

- Deben existir 15 tablas y dos vistas.
- Los conteos deben coincidir con la tabla anterior.
- Las consultas 6 a 18 del archivo de validación deben devolver cero filas.
- Las 50 facturas deben tener saldo cero.
- Deben aparecer 25 facturas con varios métodos de pago.

Las consultas vacías comprueban los datos actuales; no convierten por sí
solas una regla en una restricción automática.

## Controles adicionales de la aplicación

Esta entrega no instala automáticamente:

- La especialización total y disjunta de STAFF ni la coherencia del rol
  con la tabla hija; las consultas detectan inconsistencias.
- La obligación de agregar al menos un detalle a todo pedido.
- La generación automática de factura al entregar el pedido.
- La igualdad permanente entre factura y detalles si estos cambian.
- El bloqueo de sobrepagos o pagos de facturas anuladas.
- El descuento automático de stock.
- La prohibición de cambiar responsables o ventas ya facturadas.
- La validación del estado activo del mesero antes de operar.

Estas operaciones deben ejecutarse mediante servicios transaccionales
del backend o controles SQL adicionales.

Al cambiar la capacidad o el estado de una mesa, la aplicación también
debe revisar las reservas existentes: el trigger de reservas no se
ejecuta por modificar directamente MESA.

El responsable de los pagos se obtiene por:
PAGO → FACTURA → PEDIDO → STAFF.

## Correspondencia con Java

Los repositorios deben utilizar los nombres reales de estas columnas.

- Reserva usa idReserva como identificador simple.
- Numero de factura es INTEGER.
- Dinero debe manejarse con BigDecimal.
- Disponibilidad y doble factor se almacenan como S/N.
- Los correos y teléfonos se guardan en sus tablas separadas.
- Los estados se almacenan en mayúsculas según los CHECK.

Las clases Java por sí solas no crean las tablas ni garantizan sus FK.

## Respaldo y restauración

Seguir los comandos de 05_backup_restore.sql en una terminal.

Restaurar en una base vacía llamada savana_restaurada.
No ejecutar previamente los scripts de creación e inserción en ella.

Las extensiones requeridas deben estar disponibles en el destino.

Después de restaurar, ejecutar las consultas de validación y comparar
conteos. Conservar evidencias del proceso.

## Alcance

Esta entrega cubre los scripts y su README.
La conexión de la aplicación, instalación de VM y documentos de selección
del sistema operativo y RDBMS son entregables separados.