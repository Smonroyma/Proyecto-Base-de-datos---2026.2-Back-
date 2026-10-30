-- =========================================================
-- 05_backup_restore.sql
-- Comandos para TERMINAL, no para Query Tool.
-- Copiar cada comando SIN los guiones iniciales.
-- Las herramientas de PostgreSQL deben estar en PATH.
-- =========================================================

-- 1. RESPALDO
-- Desde database/03_scripts_varios, crear la carpeta:
-- mkdir backups

-- Generar respaldo completo de estructura y datos:
-- pg_dump -h localhost -U postgres -d savana -F c -f backups/savana.backup

-- Continuar solo si el comando termina correctamente.

-- 2. REVISAR CONTENIDO
-- pg_restore --list backups/savana.backup

-- 3. CREAR BASE VACÍA PARA LA RECUPERACIÓN
-- createdb -h localhost -U postgres -T template0 -E UTF8 savana_restaurada

-- Si ya existe, elegir otro nombre de base vacía.
-- No ejecutar los scripts 01 y 02 en esa base.

-- 4. RESTAURAR
-- pg_restore -h localhost -U postgres -d savana_restaurada --no-owner --no-privileges --exit-on-error --single-transaction backups/savana.backup

-- Las extensiones btree_gist y pgcrypto deben estar
-- disponibles en el servidor de destino.

-- 5. VERIFICAR
-- Desde esta misma carpeta:
-- psql -h localhost -U postgres -d savana_restaurada -v ON_ERROR_STOP=1 -f 03_consultas_validacion.sql

-- Comparar los conteos con la base original.
-- Guardar evidencias del respaldo y su restauración.