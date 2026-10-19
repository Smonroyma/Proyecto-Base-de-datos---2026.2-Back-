package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
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
 * Implementación de persistencia para PagoFactura (tabla pago) utilizando SQL nativo y JdbcTemplate.
 */
@Repository
public class PagoFacturaRepositoryImpl implements PagoFacturaRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT p.id_pago, p.num_factura, p.tipo_metodo_pago, mp.nombre AS nombre_metodo_pago,
                   p.fecha, p.estado, p.total, p.referencia_transaccion
            FROM pago p
            INNER JOIN metodo_pago mp ON p.tipo_metodo_pago = mp.tipo
            """;

    private final RowMapper<PagoFactura> pagoRowMapper = (rs, rowNum) -> {
        Long id;
        try {
            id = rs.getLong("id_pago");
        } catch (SQLException e) {
            id = rs.getLong(1);
        }
        Timestamp ts = rs.getTimestamp("fecha");
        return PagoFactura.builder()
                .idPago(id)
                .numFactura(rs.getString("num_factura"))
                .tipoMetodoPago(rs.getString("tipo_metodo_pago"))
                .nombreMetodoPago(rs.getString("nombre_metodo_pago"))
                .fecha(ts != null ? ts.toLocalDateTime() : null)
                .estado(rs.getString("estado"))
                .total(rs.getDouble("total"))
                .referenciaTransaccion(rs.getString("referencia_transaccion"))
                .build();
    };

    public PagoFacturaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBatch(List<PagoFactura> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO pago (num_factura, tipo_metodo_pago, fecha, estado, total, referencia_transaccion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PagoFactura pago = pagos.get(i);
                LocalDateTime fecha = pago.getFecha() != null ? pago.getFecha() : LocalDateTime.now();
                String estado = pago.getEstado() != null ? pago.getEstado().toUpperCase().trim() : "PAGADO";
                ps.setString(1, pago.getNumFactura());
                ps.setString(2, pago.getTipoMetodoPago());
                ps.setTimestamp(3, Timestamp.valueOf(fecha));
                ps.setString(4, estado);
                ps.setDouble(5, pago.getTotal());
                ps.setString(6, pago.getReferenciaTransaccion());
            }

            @Override
            public int getBatchSize() {
                return pagos.size();
            }
        });
    }

    @Override
    public List<PagoFactura> findByNumFactura(String numFactura) {
        String sql = BASE_SELECT + " WHERE UPPER(TRIM(p.num_factura)) = UPPER(TRIM(?)) ORDER BY p.id_pago ASC";
        return jdbcTemplate.query(sql, pagoRowMapper, numFactura);
    }

    @Override
    public List<PagoFactura> findAll() {
        String sql = BASE_SELECT + " ORDER BY p.fecha DESC, p.id_pago DESC";
        return jdbcTemplate.query(sql, pagoRowMapper);
    }

    @Override
    public Optional<PagoFactura> findById(Long idPago) {
        String sql = BASE_SELECT + " WHERE p.id_pago = ?";
        try {
            PagoFactura pago = jdbcTemplate.queryForObject(sql, pagoRowMapper, idPago);
            return Optional.ofNullable(pago);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
