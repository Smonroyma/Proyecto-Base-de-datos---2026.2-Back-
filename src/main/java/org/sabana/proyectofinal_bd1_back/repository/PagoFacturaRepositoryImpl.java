package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.PagoFactura;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de persistencia para PagoFactura utilizando SQL nativo y JdbcTemplate.
 */
@Repository
public class PagoFacturaRepositoryImpl implements PagoFacturaRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PagoFactura> pagoRowMapper = (rs, rowNum) -> {
        Long id = null;
        try {
            id = rs.getLong("id_pago");
        } catch (SQLException e) {
            id = rs.getLong(1);
        }
        Timestamp ts = rs.getTimestamp("fecha_hora");
        return PagoFactura.builder()
                .idPago(id)
                .idFactura(rs.getLong("id_factura"))
                .idMetodoPago(rs.getLong("id_metodo_pago"))
                .nombreMetodoPago(rs.getString("nombre_metodo_pago"))
                .monto(rs.getDouble("monto"))
                .referenciaTransaccion(rs.getString("referencia_transaccion"))
                .fechaHora(ts != null ? ts.toLocalDateTime() : null)
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
                INSERT INTO pago_factura (id_factura, id_metodo_pago, monto, referencia_transaccion, fecha_hora)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PagoFactura pago = pagos.get(i);
                LocalDateTime fecha = pago.getFechaHora() != null ? pago.getFechaHora() : LocalDateTime.now();
                ps.setLong(1, pago.getIdFactura());
                ps.setLong(2, pago.getIdMetodoPago());
                ps.setDouble(3, pago.getMonto());
                ps.setString(4, pago.getReferenciaTransaccion());
                ps.setTimestamp(5, Timestamp.valueOf(fecha));
            }

            @Override
            public int getBatchSize() {
                return pagos.size();
            }
        });
    }

    @Override
    public List<PagoFactura> findByFacturaId(Long idFactura) {
        String sql = """
                SELECT pf.id_pago, pf.id_factura, pf.id_metodo_pago, mp.nombre AS nombre_metodo_pago,
                       pf.monto, pf.referencia_transaccion, pf.fecha_hora
                FROM pago_factura pf
                LEFT JOIN metodo_pago mp ON pf.id_metodo_pago = mp.id_metodo_pago
                WHERE pf.id_factura = ?
                ORDER BY pf.id_pago ASC
                """;
        return jdbcTemplate.query(sql, pagoRowMapper, idFactura);
    }

}
