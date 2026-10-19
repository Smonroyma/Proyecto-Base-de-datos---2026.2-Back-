package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de MesaRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class MesaRepositoryImpl implements MesaRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .numeroMesa(rs.getInt("numero_mesa"))
            .capacidad(rs.getInt("capacidad"))
            .estado(rs.getString("estado"))
            .build();

    public MesaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mesa> findByNumeroMesa(Integer numeroMesa) {
        String sql = "SELECT numero_mesa, capacidad, estado FROM mesa WHERE numero_mesa = ?";
        try {
            Mesa mesa = jdbcTemplate.queryForObject(sql, mesaRowMapper, numeroMesa);
            return Optional.ofNullable(mesa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Mesa> findAll() {
        String sql = "SELECT numero_mesa, capacidad, estado FROM mesa ORDER BY numero_mesa ASC";
        return jdbcTemplate.query(sql, mesaRowMapper);
    }

    @Override
    public List<Mesa> findByEstado(String estado) {
        String sql = "SELECT numero_mesa, capacidad, estado FROM mesa WHERE UPPER(estado) = UPPER(?) ORDER BY numero_mesa ASC";
        return jdbcTemplate.query(sql, mesaRowMapper, estado);
    }

    @Override
    public Mesa save(Mesa mesa) {
        String sql = "INSERT INTO mesa (numero_mesa, capacidad, estado) VALUES (?, ?, ?)";
        String estadoNormalizado = mesa.getEstado().toUpperCase().trim();
        jdbcTemplate.update(
                sql,
                mesa.getNumeroMesa(),
                mesa.getCapacidad(),
                estadoNormalizado
        );
        mesa.setEstado(estadoNormalizado);
        return mesa;
    }

    @Override
    public Mesa update(Mesa mesa) {
        String sql = "UPDATE mesa SET capacidad = ?, estado = ? WHERE numero_mesa = ?";
        String estadoNormalizado = mesa.getEstado().toUpperCase().trim();
        jdbcTemplate.update(
                sql,
                mesa.getCapacidad(),
                estadoNormalizado,
                mesa.getNumeroMesa()
        );
        mesa.setEstado(estadoNormalizado);
        return mesa;
    }

    @Override
    public boolean updateEstado(Integer numeroMesa, String estado) {
        String sql = "UPDATE mesa SET estado = ? WHERE numero_mesa = ?";
        int rowsAffected = jdbcTemplate.update(sql, estado.toUpperCase().trim(), numeroMesa);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteByNumeroMesa(Integer numeroMesa) {
        String sql = "DELETE FROM mesa WHERE numero_mesa = ?";
        int rowsAffected = jdbcTemplate.update(sql, numeroMesa);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNumeroMesa(Integer numeroMesa) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE numero_mesa = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, numeroMesa);
        return count != null && count > 0;
    }

    @Override
    public long countByEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE UPPER(estado) = UPPER(?)";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, estado);
        return count != null ? count : 0L;
    }

}
