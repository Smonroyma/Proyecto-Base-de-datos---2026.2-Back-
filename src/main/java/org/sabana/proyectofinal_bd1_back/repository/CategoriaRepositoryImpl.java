package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Categoria> categoriaRowMapper = (rs, rowNum) -> Categoria.builder()
            .nombreCategoria(rs.getString("nombre_categoria"))
            .estado(rs.getString("estado"))
            .descripcion(rs.getString("descripcion"))
            .build();

    public CategoriaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Categoria> findByNombreCategoria(String nombreCategoria) {
        String sql = "SELECT nombre_categoria, estado, descripcion FROM categoria WHERE LOWER(TRIM(nombre_categoria)) = LOWER(TRIM(?))";
        try {
            Categoria categoria = jdbcTemplate.queryForObject(sql, categoriaRowMapper, nombreCategoria);
            return Optional.ofNullable(categoria);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Categoria> findAll() {
        String sql = "SELECT nombre_categoria, estado, descripcion FROM categoria ORDER BY nombre_categoria ASC";
        return jdbcTemplate.query(sql, categoriaRowMapper);
    }

    @Override
    public List<Categoria> findByEstado(String estado) {
        String sql = "SELECT nombre_categoria, estado, descripcion FROM categoria WHERE UPPER(estado) = UPPER(?) ORDER BY nombre_categoria ASC";
        return jdbcTemplate.query(sql, categoriaRowMapper, estado);
    }

    @Override
    public Categoria save(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre_categoria, estado, descripcion) VALUES (?, ?, ?)";
        String estadoNormalizado = categoria.getEstado() != null ? categoria.getEstado().toUpperCase().trim() : "ACTIVA";
        jdbcTemplate.update(
                sql,
                categoria.getNombreCategoria(),
                estadoNormalizado,
                categoria.getDescripcion()
        );
        categoria.setEstado(estadoNormalizado);
        return categoria;
    }

    @Override
    public Categoria update(String nombreCategoriaOriginal, Categoria categoria) {
        String sql = "UPDATE categoria SET nombre_categoria = ?, estado = ?, descripcion = ? WHERE LOWER(TRIM(nombre_categoria)) = LOWER(TRIM(?))";
        String estadoNormalizado = categoria.getEstado() != null ? categoria.getEstado().toUpperCase().trim() : "ACTIVA";
        jdbcTemplate.update(
                sql,
                categoria.getNombreCategoria(),
                estadoNormalizado,
                categoria.getDescripcion(),
                nombreCategoriaOriginal
        );
        categoria.setEstado(estadoNormalizado);
        return categoria;
    }

    @Override
    public boolean deleteByNombreCategoria(String nombreCategoria) {
        String sql = "DELETE FROM categoria WHERE LOWER(TRIM(nombre_categoria)) = LOWER(TRIM(?))";
        int rowsAffected = jdbcTemplate.update(sql, nombreCategoria);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNombreCategoria(String nombreCategoria) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nombre_categoria)) = LOWER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombreCategoria);
        return count != null && count > 0;
    }

}
