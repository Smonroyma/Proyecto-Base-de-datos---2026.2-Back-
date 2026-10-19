package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;
import org.sabana.proyectofinal_bd1_back.model.entity.Reserva;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ReservaRepository utilizando sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class ReservaRepositoryImpl implements ReservaRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT r.id_reserva, r.id_cliente,
                   CONCAT(c.nombres, ' ', c.apellidos) AS nombre_cliente,
                   c.correo AS correo_cliente,
                   r.numero_mesa, r.fecha, r.hora, r.numero_personas, r.estado, r.observaciones
            FROM reserva r
            INNER JOIN cliente c ON r.id_cliente = c.id_cliente
            INNER JOIN mesa m ON r.numero_mesa = m.numero_mesa
            """;

    private final RowMapper<Reserva> reservaRowMapper = (rs, rowNum) -> Reserva.builder()
            .idReserva(rs.getLong("id_reserva"))
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente") != null ? rs.getString("nombre_cliente").trim() : null)
            .correoCliente(rs.getString("correo_cliente"))
            .numeroMesa(rs.getObject("numero_mesa") != null ? rs.getInt("numero_mesa") : null)
            .fecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null)
            .hora(rs.getTime("hora") != null ? rs.getTime("hora").toLocalTime() : null)
            .numeroPersonas(rs.getObject("numero_personas") != null ? rs.getInt("numero_personas") : null)
            .estado(rs.getString("estado"))
            .observaciones(rs.getString("observaciones"))
            .build();

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .numeroMesa(rs.getInt("numero_mesa"))
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
        String sql = BASE_SELECT + " ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper);
    }

    @Override
    public List<Reserva> findByCliente(Long idCliente) {
        String sql = BASE_SELECT + " WHERE r.id_cliente = ? ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, idCliente);
    }

    @Override
    public List<Reserva> findByClienteCorreo(String correo) {
        String sql = BASE_SELECT + " WHERE LOWER(TRIM(c.correo)) = LOWER(TRIM(?)) ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, correo);
    }

    @Override
    public List<Reserva> findByMesa(Integer numeroMesa) {
        String sql = BASE_SELECT + " WHERE r.numero_mesa = ? ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, numeroMesa);
    }

    @Override
    public List<Reserva> findByEstado(String estado) {
        String sql = BASE_SELECT + " WHERE UPPER(r.estado) = UPPER(?) ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, estado);
    }

    @Override
    public List<Mesa> findMesasDisponibles(LocalDateTime fechaHora, Integer capacidadRequerida) {
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();
        LocalTime windowStart = hora.isBefore(LocalTime.of(2, 0)) ? LocalTime.MIN : hora.minusHours(2).plusMinutes(1);
        LocalTime windowEnd = hora.isAfter(LocalTime.of(21, 59)) ? LocalTime.MAX : hora.plusHours(2).minusMinutes(1);

        String sql = """
                SELECT m.numero_mesa, m.capacidad, m.estado
                FROM mesa m
                WHERE m.capacidad >= ?
                  AND UPPER(m.estado) <> 'EN_MANTENIMIENTO'
                  AND m.numero_mesa NOT IN (
                      SELECT r.numero_mesa
                      FROM reserva r
                      WHERE UPPER(r.estado) IN ('PENDIENTE', 'CONFIRMADA')
                        AND r.fecha = ?
                        AND r.hora BETWEEN ? AND ?
                  )
                ORDER BY m.capacidad ASC, m.numero_mesa ASC
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

    @Override
    public Reserva save(Reserva reserva) {
        String sql = """
                INSERT INTO reserva (id_cliente, numero_mesa, fecha, hora, numero_personas, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDate fecha = reserva.getFecha() != null ? reserva.getFecha() : LocalDate.now();
        LocalTime hora = reserva.getHora() != null ? reserva.getHora() : LocalTime.of(12, 0);
        String estadoUpper = reserva.getEstado() != null ? reserva.getEstado().toUpperCase().trim() : "PENDIENTE";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_reserva"});
            ps.setLong(1, reserva.getIdCliente());
            ps.setInt(2, reserva.getNumeroMesa());
            ps.setDate(3, Date.valueOf(fecha));
            ps.setTime(4, Time.valueOf(hora));
            ps.setInt(5, reserva.getNumeroPersonas());
            ps.setString(6, estadoUpper);
            ps.setString(7, reserva.getObservaciones());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            reserva.setIdReserva(generatedKey.longValue());
        }
        reserva.setFecha(fecha);
        reserva.setHora(hora);
        reserva.setEstado(estadoUpper);
        return reserva;
    }

    @Override
    public Reserva update(Reserva reserva) {
        String sql = """
                UPDATE reserva
                SET id_cliente = ?, numero_mesa = ?, fecha = ?, hora = ?, numero_personas = ?, estado = ?, observaciones = ?
                WHERE id_reserva = ?
                """;
        LocalDate fecha = reserva.getFecha() != null ? reserva.getFecha() : LocalDate.now();
        LocalTime hora = reserva.getHora() != null ? reserva.getHora() : LocalTime.of(12, 0);
        String estadoUpper = reserva.getEstado() != null ? reserva.getEstado().toUpperCase().trim() : "PENDIENTE";

        jdbcTemplate.update(
                sql,
                reserva.getIdCliente(),
                reserva.getNumeroMesa(),
                Date.valueOf(fecha),
                Time.valueOf(hora),
                reserva.getNumeroPersonas(),
                estadoUpper,
                reserva.getObservaciones(),
                reserva.getIdReserva()
        );
        reserva.setFecha(fecha);
        reserva.setHora(hora);
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
    public boolean existsOverlappingReserva(Integer numeroMesa, LocalDateTime fechaHora, Long excludeReservaId) {
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();
        LocalTime windowStart = hora.isBefore(LocalTime.of(2, 0)) ? LocalTime.MIN : hora.minusHours(2).plusMinutes(1);
        LocalTime windowEnd = hora.isAfter(LocalTime.of(21, 59)) ? LocalTime.MAX : hora.plusHours(2).minusMinutes(1);

        String sql;
        Integer count;
        if (excludeReservaId == null) {
            sql = """
                    SELECT COUNT(*) FROM reserva
                    WHERE numero_mesa = ?
                      AND UPPER(estado) IN ('PENDIENTE', 'CONFIRMADA')
                      AND fecha = ?
                      AND hora BETWEEN ? AND ?
                    """;
            count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    numeroMesa,
                    Date.valueOf(fecha),
                    Time.valueOf(windowStart),
                    Time.valueOf(windowEnd)
            );
        } else {
            sql = """
                    SELECT COUNT(*) FROM reserva
                    WHERE numero_mesa = ?
                      AND UPPER(estado) IN ('PENDIENTE', 'CONFIRMADA')
                      AND fecha = ?
                      AND hora BETWEEN ? AND ?
                      AND id_reserva <> ?
                    """;
            count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    numeroMesa,
                    Date.valueOf(fecha),
                    Time.valueOf(windowStart),
                    Time.valueOf(windowEnd),
                    excludeReservaId
            );
        }

        return count != null && count > 0;
    }

}
