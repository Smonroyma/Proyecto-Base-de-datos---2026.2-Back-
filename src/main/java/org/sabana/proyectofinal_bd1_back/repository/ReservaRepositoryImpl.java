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

    public static final int DURACION_BLOQUEO_RESERVA_HORAS = 3;

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT r.id_reserva, r.cliente_id_cliente AS id_cliente,
                   CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente,
                   (SELECT cc.correo FROM cliente_correo cc WHERE cc.cliente_id_cliente = c.id_cliente ORDER BY cc.correo LIMIT 1) AS correo_cliente,
                   r.mesa_num_mesa AS numero_mesa, r.fecha, r.hora, r.num_personas AS numero_personas, r.estado
            FROM reserva r
            INNER JOIN cliente c ON r.cliente_id_cliente = c.id_cliente
            INNER JOIN mesa m ON r.mesa_num_mesa = m.num_mesa
            """;

    private final RowMapper<Reserva> reservaRowMapper = (rs, rowNum) -> {
        Long idReserva = rs.getLong("id_reserva");
        Long idCliente = rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null;
        return Reserva.builder()
                .idReservaCompuesta(idReserva != null && idCliente != null ? idReserva + "_" + idCliente : null)
                .idReserva(idReserva)
                .idCliente(idCliente)
                .nombreCliente(rs.getString("nombre_cliente") != null ? rs.getString("nombre_cliente").trim() : null)
                .correoCliente(rs.getString("correo_cliente"))
                .numeroMesa(rs.getObject("numero_mesa") != null ? rs.getInt("numero_mesa") : null)
                .fecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null)
                .hora(rs.getTime("hora") != null ? rs.getTime("hora").toLocalTime() : null)
                .numeroPersonas(rs.getObject("numero_personas") != null ? rs.getInt("numero_personas") : null)
                .estado(rs.getString("estado"))
                .build();
    };

    private final RowMapper<Mesa> mesaRowMapper = (rs, rowNum) -> Mesa.builder()
            .numeroMesa(rs.getInt("num_mesa"))
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
        String sql = BASE_SELECT + " WHERE r.cliente_id_cliente = ? ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, idCliente);
    }

    @Override
    public List<Reserva> findByClienteCorreo(String correo) {
        String sql = BASE_SELECT + " WHERE EXISTS (SELECT 1 FROM cliente_correo cc WHERE cc.cliente_id_cliente = c.id_cliente AND LOWER(TRIM(cc.correo)) = LOWER(TRIM(?))) ORDER BY r.fecha DESC, r.hora DESC";
        return jdbcTemplate.query(sql, reservaRowMapper, correo);
    }

    @Override
    public List<Reserva> findByMesa(Integer numeroMesa) {
        String sql = BASE_SELECT + " WHERE r.mesa_num_mesa = ? ORDER BY r.fecha DESC, r.hora DESC";
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
        // Bloqueo de 3 horas (INTERVAL '3 hours') alineado con ex_reserva_sin_cruces en PostgreSQL
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

    @Override
    public Reserva save(Reserva reserva) {
        String sql = """
                INSERT INTO reserva (cliente_id_cliente, mesa_num_mesa, fecha, hora, num_personas, estado)
                VALUES (?, ?, ?, ?, ?, ?)
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
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            reserva.setIdReserva(generatedKey.longValue());
        }
        reserva.unirClaveCompuesta(reserva.getIdReserva(), reserva.getIdCliente());
        reserva.setFecha(fecha);
        reserva.setHora(hora);
        reserva.setEstado(estadoUpper);
        return reserva;
    }

    @Override
    public Reserva update(Reserva reserva) {
        String sql = """
                UPDATE reserva
                SET cliente_id_cliente = ?, mesa_num_mesa = ?, fecha = ?, hora = ?, num_personas = ?, estado = ?
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
        // Bloqueo de 3 horas (INTERVAL '3 hours') alineado con ex_reserva_sin_cruces en PostgreSQL
        LocalTime windowStart = hora.isBefore(LocalTime.of(DURACION_BLOQUEO_RESERVA_HORAS, 0))
                ? LocalTime.MIN
                : hora.minusHours(DURACION_BLOQUEO_RESERVA_HORAS).plusMinutes(1);
        LocalTime windowEnd = hora.isAfter(LocalTime.of(24 - DURACION_BLOQUEO_RESERVA_HORAS, 59))
                ? LocalTime.MAX
                : hora.plusHours(DURACION_BLOQUEO_RESERVA_HORAS).minusMinutes(1);

        String sql;
        Integer count;
        if (excludeReservaId == null) {
            sql = """
                    SELECT COUNT(*) FROM reserva
                    WHERE mesa_num_mesa = ?
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
                    WHERE mesa_num_mesa = ?
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
