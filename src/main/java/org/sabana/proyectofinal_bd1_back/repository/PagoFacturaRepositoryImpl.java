package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Pago;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para Pago (tabla pago) utilizando SQL nativo y JdbcTemplate.
 */
@Repository
public class PagoFacturaRepositoryImpl implements PagoFacturaRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT p.id_pago, p.factura_num_factura AS num_factura, p.metodo_pago_tipo AS tipo_metodo_pago,
                   p.fecha, p.estado, p.monto
            FROM pago p
            INNER JOIN metodo_pago mp ON p.metodo_pago_tipo = mp.tipo
            """;

    private final RowMapper<Pago> pagoRowMapper = (rs, rowNum) -> {
        Long id;
        try {
            id = rs.getLong("id_pago");
        } catch (SQLException e) {
            id = rs.getLong(1);
        }
        Timestamp ts = rs.getTimestamp("fecha");
        return Pago.builder()
                .idPago(id)
                .numFactura(rs.getString("num_factura"))
                .tipoMetodoPago(rs.getString("tipo_metodo_pago"))
                .fecha(ts != null ? ts.toLocalDateTime() : null)
                .estado(rs.getString("estado"))
                .monto(rs.getDouble("monto"))
                .build();
    };

    public PagoFacturaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBatch(List<Pago> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO pago (factura_num_factura, metodo_pago_tipo, fecha, estado, monto)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Pago pago = pagos.get(i);
                LocalDateTime fecha = pago.getFecha() != null ? pago.getFecha() : LocalDateTime.now();
                String estado = pago.getEstado() != null ? pago.getEstado().toUpperCase().trim() : "PAGADO";
                ps.setString(1, pago.getNumFactura());
                ps.setString(2, pago.getTipoMetodoPago());
                ps.setTimestamp(3, Timestamp.valueOf(fecha));
                ps.setString(4, estado);
                ps.setDouble(5, pago.getMonto());
            }

            @Override
            public int getBatchSize() {
                return pagos.size();
            }
        });
    }

    @Override
    public List<Pago> findByNumFactura(String numFactura) {
        String sql = BASE_SELECT + " WHERE UPPER(TRIM(p.factura_num_factura)) = UPPER(TRIM(?)) ORDER BY p.id_pago ASC";
        return jdbcTemplate.query(sql, pagoRowMapper, numFactura);
    }

    @Override
    public List<Pago> findAll() {
        String sql = BASE_SELECT + " ORDER BY p.fecha DESC, p.id_pago DESC";
        return jdbcTemplate.query(sql, pagoRowMapper);
    }

    @Override
    public Optional<Pago> findById(Long idPago) {
        String sql = BASE_SELECT + " WHERE p.id_pago = ?";
        try {
            Pago pago = jdbcTemplate.queryForObject(sql, pagoRowMapper, idPago);
            return Optional.ofNullable(pago);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
