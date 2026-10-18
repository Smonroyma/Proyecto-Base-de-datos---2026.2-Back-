package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
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
 * Implementación de MetodoPagoRepository utilizando sentencias SQL nativas puras y JdbcTemplate.
 */
@Repository
public class MetodoPagoRepositoryImpl implements MetodoPagoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<MetodoPago> metodoPagoRowMapper = (rs, rowNum) -> MetodoPago.builder()
            .idMetodoPago(rs.getLong("id_metodo_pago"))
            .codigo(rs.getString("codigo"))
            .nombre(rs.getString("nombre"))
            .descripcion(rs.getString("descripcion"))
            .activo(rs.getBoolean("activo"))
            .build();

    public MetodoPagoRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<MetodoPago> findById(Long id) {
        String sql = "SELECT id_metodo_pago, codigo, nombre, descripcion, activo FROM metodo_pago WHERE id_metodo_pago = ?";
        try {
            MetodoPago metodoPago = jdbcTemplate.queryForObject(sql, metodoPagoRowMapper, id);
            return Optional.ofNullable(metodoPago);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MetodoPago> findAll() {
        String sql = "SELECT id_metodo_pago, codigo, nombre, descripcion, activo FROM metodo_pago ORDER BY nombre ASC";
        return jdbcTemplate.query(sql, metodoPagoRowMapper);
    }

    @Override
    public List<MetodoPago> findAllActivos() {
        String sql = "SELECT id_metodo_pago, codigo, nombre, descripcion, activo FROM metodo_pago WHERE activo = true ORDER BY nombre ASC";
        return jdbcTemplate.query(sql, metodoPagoRowMapper);
    }

    @Override
    public Optional<MetodoPago> findByCodigo(String codigo) {
        String sql = "SELECT id_metodo_pago, codigo, nombre, descripcion, activo FROM metodo_pago WHERE UPPER(TRIM(codigo)) = UPPER(TRIM(?))";
        try {
            MetodoPago metodoPago = jdbcTemplate.queryForObject(sql, metodoPagoRowMapper, codigo);
            return Optional.ofNullable(metodoPago);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public MetodoPago save(MetodoPago metodoPago) {
        String sql = "INSERT INTO metodo_pago (codigo, nombre, descripcion, activo) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String codigoUpper = metodoPago.getCodigo() != null ? metodoPago.getCodigo().trim().toUpperCase() : "";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_metodo_pago" });
            ps.setString(1, codigoUpper);
            ps.setString(2, metodoPago.getNombre());
            ps.setString(3, metodoPago.getDescripcion());
            ps.setBoolean(4, Boolean.TRUE.equals(metodoPago.getActivo()));
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            metodoPago.setIdMetodoPago(generatedKey.longValue());
        }
        metodoPago.setCodigo(codigoUpper);
        return metodoPago;
    }

    @Override
    public MetodoPago update(MetodoPago metodoPago) {
        String sql = "UPDATE metodo_pago SET codigo = ?, nombre = ?, descripcion = ?, activo = ? WHERE id_metodo_pago = ?";
        String codigoUpper = metodoPago.getCodigo() != null ? metodoPago.getCodigo().trim().toUpperCase() : "";

        jdbcTemplate.update(
                sql,
                codigoUpper,
                metodoPago.getNombre(),
                metodoPago.getDescripcion(),
                Boolean.TRUE.equals(metodoPago.getActivo()),
                metodoPago.getIdMetodoPago()
        );
        metodoPago.setCodigo(codigoUpper);
        return metodoPago;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM metodo_pago WHERE id_metodo_pago = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM metodo_pago WHERE UPPER(TRIM(codigo)) = UPPER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, codigo);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCodigoAndNotId(String codigo, Long id) {
        String sql = "SELECT COUNT(*) FROM metodo_pago WHERE UPPER(TRIM(codigo)) = UPPER(TRIM(?)) AND id_metodo_pago <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, codigo, id);
        return count != null && count > 0;
    }

}
