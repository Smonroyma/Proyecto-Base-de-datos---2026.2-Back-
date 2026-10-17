package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
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
 * Implementación de MesaRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class MesaRepositoryImpl implements MesaRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .idMesa(rs.getLong("id_mesa"))
            .numero(rs.getInt("numero"))
            .capacidad(rs.getInt("capacidad"))
            .estado(rs.getString("estado"))
            .build();

    public MesaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mesa> findById(Long id) {
        String sql = "SELECT id_mesa, numero, capacidad, estado FROM mesa WHERE id_mesa = ?";
        try {
            Mesa mesa = jdbcTemplate.queryForObject(sql, mesaRowMapper, id);
            return Optional.ofNullable(mesa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Mesa> findAll() {
        String sql = "SELECT id_mesa, numero, capacidad, estado FROM mesa ORDER BY numero ASC";
        return jdbcTemplate.query(sql, mesaRowMapper);
    }

    @Override
    public List<Mesa> findByEstado(String estado) {
        String sql = "SELECT id_mesa, numero, capacidad, estado FROM mesa WHERE UPPER(estado) = UPPER(?) ORDER BY numero ASC";
        return jdbcTemplate.query(sql, mesaRowMapper, estado);
    }

    @Override
    public Optional<Mesa> findByNumero(Integer numero) {
        String sql = "SELECT id_mesa, numero, capacidad, estado FROM mesa WHERE numero = ?";
        try {
            Mesa mesa = jdbcTemplate.queryForObject(sql, mesaRowMapper, numero);
            return Optional.ofNullable(mesa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Mesa save(Mesa mesa) {
        String sql = "INSERT INTO mesa (numero, capacidad, estado) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_mesa" });
            ps.setInt(1, mesa.getNumero());
            ps.setInt(2, mesa.getCapacidad());
            ps.setString(3, mesa.getEstado().toUpperCase());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            mesa.setIdMesa(generatedKey.longValue());
        }
        mesa.setEstado(mesa.getEstado().toUpperCase());
        return mesa;
    }

    @Override
    public Mesa update(Mesa mesa) {
        String sql = "UPDATE mesa SET numero = ?, capacidad = ?, estado = ? WHERE id_mesa = ?";
        jdbcTemplate.update(
                sql,
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado().toUpperCase(),
                mesa.getIdMesa()
        );
        mesa.setEstado(mesa.getEstado().toUpperCase());
        return mesa;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM mesa WHERE id_mesa = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNumero(Integer numero) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE numero = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, numero);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNumeroAndNotId(Integer numero, Long id) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE numero = ? AND id_mesa <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, numero, id);
        return count != null && count > 0;
    }

    @Override
    public long countByEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE UPPER(estado) = UPPER(?)";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, estado);
        return count != null ? count : 0L;
    }

}
