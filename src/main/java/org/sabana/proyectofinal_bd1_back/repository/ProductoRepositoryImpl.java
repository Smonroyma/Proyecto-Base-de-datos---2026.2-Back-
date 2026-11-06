package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Producto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositoryImpl implements ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.disponibilidad,
                   p.stock, p.imagen_url, p.categoria_nombre_categoria AS nombre_categoria
            FROM producto p
            INNER JOIN categoria c ON p.categoria_nombre_categoria = c.nombre_categoria
            """;

    private final RowMapper<Producto> productoRowMapper = (rs, rowNum) -> Producto.builder()
            .idProducto(rs.getLong("id_producto"))
            .nombre(rs.getString("nombre"))
            .descripcion(rs.getString("descripcion"))
            .precio(rs.getDouble("precio"))
            .disponibilidad(rs.getBoolean("disponibilidad"))
            .stock(rs.getInt("stock"))
            .imagenUrl(rs.getString("imagen_url"))
            .nombreCategoria(rs.getString("nombre_categoria"))
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
    public List<Producto> findByCategoria(String nombreCategoria) {
        String sql = BASE_SELECT + " WHERE LOWER(TRIM(p.categoria_nombre_categoria)) = LOWER(TRIM(?)) ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, nombreCategoria);
    }

    @Override
    public List<Producto> findByDisponibilidad(Boolean disponibilidad) {
        String sql = BASE_SELECT + " WHERE p.disponibilidad = ? ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, disponibilidad);
    }

    @Override
    public List<Producto> findByCategoriaAndDisponibilidad(String nombreCategoria, Boolean disponibilidad) {
        String sql = BASE_SELECT + " WHERE LOWER(TRIM(p.categoria_nombre_categoria)) = LOWER(TRIM(?)) AND p.disponibilidad = ? ORDER BY p.id_producto ASC";
        return jdbcTemplate.query(sql, productoRowMapper, nombreCategoria, disponibilidad);
    }

    @Override
    public Producto save(Producto producto) {
        String sql = """
                INSERT INTO producto (nombre, descripcion, precio, disponibilidad, stock, imagen_url, categoria_nombre_categoria)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_producto"});
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setBoolean(4, producto.getDisponibilidad() != null && producto.getDisponibilidad());
            ps.setInt(5, producto.getStock() != null ? producto.getStock() : 0);
            ps.setString(6, producto.getImagenUrl());
            ps.setString(7, producto.getNombreCategoria());
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
                SET nombre = ?, descripcion = ?, precio = ?, disponibilidad = ?, stock = ?, imagen_url = ?, categoria_nombre_categoria = ?
                WHERE id_producto = ?
                """;
        jdbcTemplate.update(
                sql,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getDisponibilidad(),
                producto.getStock(),
                producto.getImagenUrl(),
                producto.getNombreCategoria(),
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

    @Override
    public boolean updateStock(Long idProducto, Integer nuevoStock) {
        String sql = "UPDATE producto SET stock = ? WHERE id_producto = ?";
        int rowsAffected = jdbcTemplate.update(sql, nuevoStock, idProducto);
        return rowsAffected > 0;
    }

}
