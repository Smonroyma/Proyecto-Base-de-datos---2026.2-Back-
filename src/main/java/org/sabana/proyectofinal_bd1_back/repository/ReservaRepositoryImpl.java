package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Reserva;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ReservaRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class ReservaRepositoryImpl implements ReservaRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT r.id_reserva, r.id_cliente, c.nombre AS nombre_cliente,
                   r.id_mesa, m.numero AS numero_mesa,
                   r.fecha_hora, r.numero_personas, r.estado, r.observaciones
            FROM reserva r
            LEFT JOIN cliente c ON r.id_cliente = c.id_cliente
            LEFT JOIN mesa m ON r.id_mesa = m.id_mesa
            """;

    private final RowMapper<Reserva> reservaRowMapper = (rs, rowNum) -> Reserva.builder()
            .idReserva(rs.getLong("id_reserva"))
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente"))
            .idMesa(rs.getObject("id_mesa") != null ? rs.getLong("id_mesa") : null)
            .numeroMesa(rs.getObject("numero_mesa") != null ? rs.getInt("numero_mesa") : null)
            .fechaHora(rs.getTimestamp("fecha_hora") != null ? rs.getTimestamp("fecha_hora").toLocalDateTime() : null)
            .numeroPersonas(rs.getObject("numero_personas") != null ? rs.getInt("numero_personas") : null)
            .estado(rs.getString("estado"))
            .observaciones(rs.getString("observaciones"))
            .build();

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .idMesa(rs.getLong("id_mesa"))
            .numero(rs.getInt("numero"))
            .capacidad(rs.getInt("capacidad"))
            .estado(rs.getString("estado"))
            .build();

    public ReservaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Reserva> findById(Long id) {
        String sql = BASE_SELECT + " WHERE r.id_reserva = ?";
        try {
            Reserva reserva = jdbcTemplate.queryForObject(sql, reservaRowMapper, id);
            return Optional.ofNullable(reserva);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reserva> findAll() {
        String sql = BASE_SELECT + " ORDER BY r.fecha_hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper);
    }

    @Override
    public List<Reserva> findByCliente(Long idCliente) {
        String sql = BASE_SELECT + " WHERE r.id_cliente = ? ORDER BY r.fecha_hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, idCliente);
    }

    @Override
    public List<Reserva> findByMesa(Long idMesa) {
        String sql = BASE_SELECT + " WHERE r.id_mesa = ? ORDER BY r.fecha_hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, idMesa);
    }

    @Override
    public List<Reserva> findByEstado(String estado) {
        String sql = BASE_SELECT + " WHERE UPPER(r.estado) = UPPER(?) ORDER BY r.fecha_hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, estado);
    }

    @Override
    public List<Mesa> findMesasDisponibles(LocalDateTime fechaHora, Integer capacidadRequerida) {
        Timestamp windowStart = Timestamp.valueOf(fechaHora.minusHours(2).plusMinutes(1));
        Timestamp windowEnd = Timestamp.valueOf(fechaHora.plusHours(2).minusMinutes(1));

        String sql = """
                SELECT m.id_mesa, m.numero, m.capacidad, m.estado
                FROM mesa m
                WHERE m.capacidad >= ?
                  AND UPPER(m.estado) <> 'EN_MANTENIMIENTO'
                  AND m.id_mesa NOT IN (
                      SELECT r.id_mesa
                      FROM reserva r
                      WHERE UPPER(r.estado) IN ('PENDIENTE', 'CONFIRMADA')
                        AND r.fecha_hora BETWEEN ? AND ?
                  )
                ORDER BY m.capacidad ASC, m.numero ASC
                """;

        return jdbcTemplate.query(sql, mesaRowMapper, capacidadRequerida, windowStart, windowEnd);
    }

    @Override
    public Reserva save(Reserva reserva) {
        String sql = """
                INSERT INTO reserva (id_cliente, id_mesa, fecha_hora, numero_personas, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_reserva" });
            ps.setLong(1, reserva.getIdCliente());
            ps.setLong(2, reserva.getIdMesa());
            ps.setTimestamp(3, Timestamp.valueOf(reserva.getFechaHora()));
            ps.setInt(4, reserva.getNumeroPersonas());
            ps.setString(5, reserva.getEstado() != null ? reserva.getEstado().toUpperCase() : "PENDIENTE");
            ps.setString(6, reserva.getObservaciones());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            reserva.setIdReserva(generatedKey.longValue());
        }
        if (reserva.getEstado() != null) {
            reserva.setEstado(reserva.getEstado().toUpperCase());
        } else {
            reserva.setEstado("PENDIENTE");
        }
        return reserva;
    }

    @Override
    public Reserva update(Reserva reserva) {
        String sql = """
                UPDATE reserva
                SET id_cliente = ?, id_mesa = ?, fecha_hora = ?, numero_personas = ?, estado = ?, observaciones = ?
                WHERE id_reserva = ?
                """;
        String estadoUpper = reserva.getEstado() != null ? reserva.getEstado().toUpperCase() : "PENDIENTE";
        jdbcTemplate.update(
                sql,
                reserva.getIdCliente(),
                reserva.getIdMesa(),
                Timestamp.valueOf(reserva.getFechaHora()),
                reserva.getNumeroPersonas(),
                estadoUpper,
                reserva.getObservaciones(),
                reserva.getIdReserva()
        );
        reserva.setEstado(estadoUpper);
        return reserva;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM reserva WHERE id_reserva = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsOverlappingReserva(Long idMesa, LocalDateTime fechaHora, Long excludeReservaId) {
        Timestamp windowStart = Timestamp.valueOf(fechaHora.minusHours(2).plusMinutes(1));
        Timestamp windowEnd = Timestamp.valueOf(fechaHora.plusHours(2).minusMinutes(1));

        String sql;
        Integer count;
        if (excludeReservaId == null) {
            sql = """
                    SELECT COUNT(*) FROM reserva
                    WHERE id_mesa = ?
                      AND UPPER(estado) IN ('PENDIENTE', 'CONFIRMADA')
                      AND fecha_hora BETWEEN ? AND ?
                    """;
            count = jdbcTemplate.queryForObject(sql, Integer.class, idMesa, windowStart, windowEnd);
        } else {
            sql = """
                    SELECT COUNT(*) FROM reserva
                    WHERE id_mesa = ?
                      AND UPPER(estado) IN ('PENDIENTE', 'CONFIRMADA')
                      AND fecha_hora BETWEEN ? AND ?
                      AND id_reserva <> ?
                    """;
            count = jdbcTemplate.queryForObject(sql, Integer.class, idMesa, windowStart, windowEnd, excludeReservaId);
        }

        return count != null && count > 0;
    }

}
