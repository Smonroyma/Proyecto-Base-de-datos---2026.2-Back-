package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de CategoriaRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Categoria> categoriaRowMapper = (rs, rowNum) -> Categoria.builder()
            .idCategoria(rs.getLong("id_categoria"))
            .nombre(rs.getString("nombre"))
            .descripcion(rs.getString("descripcion"))
            .activo(rs.getBoolean("activo"))
            .build();

    public CategoriaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        String sql = "SELECT id_categoria, nombre, descripcion, activo FROM categoria WHERE id_categoria = ?";
        try {
            Categoria categoria = jdbcTemplate.queryForObject(sql, categoriaRowMapper, id);
            return Optional.ofNullable(categoria);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Categoria> findAll() {
        String sql = "SELECT id_categoria, nombre, descripcion, activo FROM categoria ORDER BY nombre ASC";
        return jdbcTemplate.query(sql, categoriaRowMapper);
    }

    @Override
    public List<Categoria> findAllActivas() {
        String sql = "SELECT id_categoria, nombre, descripcion, activo FROM categoria WHERE activo = true ORDER BY nombre ASC";
        return jdbcTemplate.query(sql, categoriaRowMapper);
    }

    @Override
    public Categoria save(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion, activo) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_categoria" });
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setBoolean(3, categoria.getActivo() != null && categoria.getActivo());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            categoria.setIdCategoria(generatedKey.longValue());
        }
        return categoria;
    }

    @Override
    public Categoria update(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ?, activo = ? WHERE id_categoria = ?";
        jdbcTemplate.update(
                sql,
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo(),
                categoria.getIdCategoria()
        );
        return categoria;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNombreAndNotId(String nombre, Long id) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_categoria <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre, id);
        return count != null && count > 0;
    }

}
