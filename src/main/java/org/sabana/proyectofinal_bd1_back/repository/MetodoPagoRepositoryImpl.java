package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MetodoPagoRepositoryImpl implements MetodoPagoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<MetodoPago> metodoPagoRowMapper = (rs, rowNum) -> MetodoPago.builder()
            .tipo(rs.getString("tipo"))
            .build();

    public MetodoPagoRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<MetodoPago> findByTipo(String tipo) {
        String sql = "SELECT tipo FROM metodo_pago WHERE UPPER(TRIM(tipo)) = UPPER(TRIM(?))";
        try {
            MetodoPago metodoPago = jdbcTemplate.queryForObject(sql, metodoPagoRowMapper, tipo);
            return Optional.ofNullable(metodoPago);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MetodoPago> findAll() {
        String sql = "SELECT tipo FROM metodo_pago ORDER BY tipo ASC";
        return jdbcTemplate.query(sql, metodoPagoRowMapper);
    }

    @Override
    public List<MetodoPago> findAllActivos() {
        String sql = "SELECT tipo FROM metodo_pago ORDER BY tipo ASC";
        return jdbcTemplate.query(sql, metodoPagoRowMapper);
    }

    @Override
    public MetodoPago save(MetodoPago metodoPago) {
        String sql = "INSERT INTO metodo_pago (tipo) VALUES (?)";
        String tipoUpper = metodoPago.getTipo() != null ? metodoPago.getTipo().trim().toUpperCase() : "";
        jdbcTemplate.update(sql, tipoUpper);
        metodoPago.setTipo(tipoUpper);
        return metodoPago;
    }

    @Override
    public MetodoPago update(String tipoOriginal, MetodoPago metodoPago) {
        String sql = "UPDATE metodo_pago SET tipo = ? WHERE UPPER(TRIM(tipo)) = UPPER(TRIM(?))";
        String tipoUpper = metodoPago.getTipo() != null ? metodoPago.getTipo().trim().toUpperCase() : "";
        jdbcTemplate.update(sql, tipoUpper, tipoOriginal);
        metodoPago.setTipo(tipoUpper);
        return metodoPago;
    }

    @Override
    public boolean deleteByTipo(String tipo) {
        String sql = "DELETE FROM metodo_pago WHERE UPPER(TRIM(tipo)) = UPPER(TRIM(?))";
        int rowsAffected = jdbcTemplate.update(sql, tipo);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByTipo(String tipo) {
        String sql = "SELECT COUNT(*) FROM metodo_pago WHERE UPPER(TRIM(tipo)) = UPPER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tipo);
        return count != null && count > 0;
    }

}
