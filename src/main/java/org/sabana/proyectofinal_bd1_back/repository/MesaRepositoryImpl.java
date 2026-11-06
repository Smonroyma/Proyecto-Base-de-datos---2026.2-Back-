package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MesaRepositoryImpl implements MesaRepository {

    public static final int DURACION_BLOQUEO_RESERVA_HORAS = 3;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .numeroMesa(rs.getInt("num_mesa"))
            .capacidad(rs.getInt("capacidad"))
            .estado(rs.getString("estado"))
            .build();

    public MesaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mesa> findByNumeroMesa(Integer numeroMesa) {
        String sql = "SELECT num_mesa, capacidad, estado FROM mesa WHERE num_mesa = ?";
        try {
            Mesa mesa = jdbcTemplate.queryForObject(sql, mesaRowMapper, numeroMesa);
            return Optional.ofNullable(mesa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Mesa> findAll() {
        String sql = "SELECT num_mesa, capacidad, estado FROM mesa ORDER BY num_mesa ASC";
        return jdbcTemplate.query(sql, mesaRowMapper);
    }

    @Override
    public List<Mesa> findByEstado(String estado) {
        String sql = "SELECT num_mesa, capacidad, estado FROM mesa WHERE UPPER(estado) = UPPER(?) ORDER BY num_mesa ASC";
        return jdbcTemplate.query(sql, mesaRowMapper, estado);
    }

    @Override
    public Mesa save(Mesa mesa) {
        String sql = "INSERT INTO mesa (num_mesa, capacidad, estado) VALUES (?, ?, ?)";
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
        String sql = "UPDATE mesa SET capacidad = ?, estado = ? WHERE num_mesa = ?";
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
        String sql = "UPDATE mesa SET estado = ? WHERE num_mesa = ?";
        int rowsAffected = jdbcTemplate.update(sql, estado.toUpperCase().trim(), numeroMesa);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteByNumeroMesa(Integer numeroMesa) {
        String sql = "DELETE FROM mesa WHERE num_mesa = ?";
        int rowsAffected = jdbcTemplate.update(sql, numeroMesa);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNumeroMesa(Integer numeroMesa) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE num_mesa = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, numeroMesa);
        return count != null && count > 0;
    }

    @Override
    public long countByEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM mesa WHERE UPPER(estado) = UPPER(?)";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, estado);
        return count != null ? count : 0L;
    }

    public List<Mesa> findMesasDisponiblesPorHorario(LocalDateTime fechaHora, Integer capacidadRequerida) {
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();
        LocalTime windowStart = hora.isBefore(LocalTime.of(DURACION_BLOQUEO_RESERVA_HORAS, 0))
                ? LocalTime.MIN
                : hora.minusHours(DURACION_BLOQUEO_RESERVA_HORAS).plusMinutes(1);
        LocalTime windowEnd = hora.isAfter(LocalTime.of(24 - DURACION_BLOQUEO_RESERVA_HORAS, 59))
                ? LocalTime.MAX
                : hora.plusHours(DURACION_BLOQUEO_RESERVA_HORAS).minusMinutes(1);

        String sql = """
                SELECT m.num_mesa, m.capacidad, m.estado
                FROM mesa m
                WHERE m.capacidad >= ?
                  AND UPPER(m.estado) <> 'EN_MANTENIMIENTO'
                  AND m.num_mesa NOT IN (
                      SELECT r.mesa_num_mesa
                      FROM reserva r
                      WHERE UPPER(r.estado) IN ('PENDIENTE', 'CONFIRMADA')
                        AND r.fecha = ?
                        AND r.hora BETWEEN ? AND ?
                  )
                ORDER BY m.capacidad ASC, m.num_mesa ASC
                """;
        return jdbcTemplate.query(
                sql,
                mesaRowMapper,
                capacidadRequerida,
                Date.valueOf(fecha),
                Time.valueOf(windowStart),
                Time.valueOf(windowEnd)
        );
    }

}
