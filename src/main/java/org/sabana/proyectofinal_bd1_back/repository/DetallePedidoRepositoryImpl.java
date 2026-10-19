package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación de persistencia para DetallePedido (detalle_pedido_contener) utilizando SQL nativo y JdbcTemplate.
 */
@Repository
public class DetallePedidoRepositoryImpl implements DetallePedidoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DetallePedido> detalleRowMapper = (rs, rowNum) -> {
        Long id;
        try {
            id = rs.getLong("id_detalle_pedido");
        } catch (SQLException e) {
            id = rs.getLong(1);
        }
        return DetallePedido.builder()
                .idDetallePedido(id)
                .idPedido(rs.getLong("id_pedido"))
                .idProducto(rs.getLong("id_producto"))
                .nombreProducto(rs.getString("nombre_producto"))
                .cantidad(rs.getInt("cantidad"))
                .precioUnitario(rs.getDouble("precio_unitario"))
                .subtotal(rs.getDouble("subtotal"))
                .build();
    };

    public DetallePedidoRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBatch(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO detalle_pedido_contener (id_pedido, id_producto, cantidad, precio_unitario, subtotal)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DetallePedido detalle = detalles.get(i);
                ps.setLong(1, detalle.getIdPedido());
                ps.setLong(2, detalle.getIdProducto());
                ps.setInt(3, detalle.getCantidad());
                ps.setDouble(4, detalle.getPrecioUnitario());
                ps.setDouble(5, detalle.getSubtotal());
            }

            @Override
            public int getBatchSize() {
                return detalles.size();
            }
        });
    }

    @Override
    public List<DetallePedido> findByPedidoId(Long idPedido) {
        String sql = """
                SELECT d.id_detalle_pedido, d.id_pedido, d.id_producto, p.nombre AS nombre_producto,
                       d.cantidad, d.precio_unitario, d.subtotal
                FROM detalle_pedido_contener d
                INNER JOIN producto p ON d.id_producto = p.id_producto
                WHERE d.id_pedido = ?
                ORDER BY d.id_detalle_pedido ASC
                """;
        return jdbcTemplate.query(sql, detalleRowMapper, idPedido);
    }

    @Override
    public boolean deleteByPedidoId(Long idPedido) {
        String sql = "DELETE FROM detalle_pedido_contener WHERE id_pedido = ?";
        int rowsAffected = jdbcTemplate.update(sql, idPedido);
        return rowsAffected > 0;
    }

}
