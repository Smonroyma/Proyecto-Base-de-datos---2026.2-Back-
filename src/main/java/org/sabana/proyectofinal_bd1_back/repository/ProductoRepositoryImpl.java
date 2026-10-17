package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Producto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ProductoRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class ProductoRepositoryImpl implements ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.id_categoria,\s
                   c.nombre AS nombre_categoria, p.imagen_url, p.disponible, p.stock
            FROM producto p
            LEFT JOIN categoria c ON p.id_categoria = c.id_categoria
            """;

    private final RowMapper<Producto> productoRowMapper = (rs, rowNum) -> Producto.builder()
            .idProducto(rs.getLong("id_producto"))
            .nombre(rs.getString("nombre"))
            .descripcion(rs.getString("descripcion"))
            .precio(rs.getDouble("precio"))
            .idCategoria(rs.getObject("id_categoria") != null ? rs.getLong("id_categoria") : null)
            .nombreCategoria(rs.getString("nombre_categoria"))
            .imagenUrl(rs.getString("imagen_url"))
            .disponible(rs.getBoolean("disponible"))
            .stock(rs.getInt("stock"))
            .build();

    public ProductoRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Producto> findById(Long id) {
        String sql = BASE_SELECT + " WHERE p.id_producto = ?";
        try {
            Producto producto = jdbcTemplate.queryForObject(sql, productoRowMapper, id);
            return Optional.ofNullable(producto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Producto> findAll() {
        String sql = BASE_SELECT + " ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper);
    }

    @Override
    public List<Producto> findByCategoria(Long idCategoria) {
        String sql = BASE_SELECT + " WHERE p.id_categoria = ? ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, idCategoria);
    }

    @Override
    public List<Producto> findByDisponible(Boolean disponible) {
        String sql = BASE_SELECT + " WHERE p.disponible = ? ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, disponible);
    }

    @Override
    public List<Producto> findByCategoriaAndDisponible(Long idCategoria, Boolean disponible) {
        String sql = BASE_SELECT + " WHERE p.id_categoria = ? AND p.disponible = ? ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, idCategoria, disponible);
    }

    @Override
    public Producto save(Producto producto) {
        String sql = """
                INSERT INTO producto (nombre, descripcion, precio, id_categoria, imagen_url, disponible, stock)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_producto" });
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            if (producto.getIdCategoria() != null) {
                ps.setLong(4, producto.getIdCategoria());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, producto.getImagenUrl());
            ps.setBoolean(6, producto.getDisponible() != null && producto.getDisponible());
            ps.setInt(7, producto.getStock() != null ? producto.getStock() : 0);
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            producto.setIdProducto(generatedKey.longValue());
        }
        return producto;
    }

    @Override
    public Producto update(Producto producto) {
        String sql = """
                UPDATE producto
                SET nombre = ?, descripcion = ?, precio = ?, id_categoria = ?, imagen_url = ?, disponible = ?, stock = ?
                WHERE id_producto = ?
                """;
        jdbcTemplate.update(
                sql,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getIdCategoria(),
                producto.getImagenUrl(),
                producto.getDisponible(),
                producto.getStock(),
                producto.getIdProducto()
        );
        return producto;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM producto WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNombreAndNotId(String nombre, Long id) {
        String sql = "SELECT COUNT(*) FROM producto WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_producto <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre, id);
        return count != null && count > 0;
    }

}
