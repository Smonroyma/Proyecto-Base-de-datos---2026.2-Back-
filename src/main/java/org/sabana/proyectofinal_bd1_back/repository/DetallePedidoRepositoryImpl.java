package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para DetallePedido (tabla 'detalle_pedido')
 * utilizando SQL nativo puro y JdbcTemplate con clave compuesta (id_pedido, id_producto).
 */
@Repository
public class DetallePedidoRepositoryImpl implements DetallePedidoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DetallePedido> detalleRowMapper = (rs, rowNum) -> DetallePedido.builder()
            .idPedido(rs.getLong("id_pedido"))
            .idProducto(rs.getLong("id_producto"))
            .nombreProducto(rs.getString("nombre_producto"))
            .cantidad(rs.getInt("cantidad"))
            .precioUnitario(rs.getObject("precio_unitario") != null ? rs.getDouble("precio_unitario") : 0.0)
            .build();

    public DetallePedidoRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBatch(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DetallePedido detalle = detalles.get(i);
                ps.setLong(1, detalle.getIdPedido());
                ps.setLong(2, detalle.getIdProducto());
                ps.setInt(3, detalle.getCantidad());
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
                SELECT d.id_pedido, d.id_producto, p.nombre AS nombre_producto,
                       d.cantidad, p.precio AS precio_unitario
                FROM detalle_pedido d
                INNER JOIN producto p ON d.id_producto = p.id_producto
                WHERE d.id_pedido = ?
                ORDER BY d.id_producto ASC
                """;
        return jdbcTemplate.query(sql, detalleRowMapper, idPedido);
    }

    @Override
    public Optional<DetallePedido> findByIdPedidoAndIdProducto(Long idPedido, Long idProducto) {
        String sql = """
                SELECT d.id_pedido, d.id_producto, p.nombre AS nombre_producto,
                       d.cantidad, p.precio AS precio_unitario
                FROM detalle_pedido d
                INNER JOIN producto p ON d.id_producto = p.id_producto
                WHERE d.id_pedido = ? AND d.id_producto = ?
                """;
        try {
            DetallePedido detalle = jdbcTemplate.queryForObject(sql, detalleRowMapper, idPedido, idProducto);
            return Optional.ofNullable(detalle);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean updateCantidad(Long idPedido, Long idProducto, int cantidad) {
        String sql = "UPDATE detalle_pedido SET cantidad = ? WHERE id_pedido = ? AND id_producto = ?";
        int rowsAffected = jdbcTemplate.update(sql, cantidad, idPedido, idProducto);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteByIdPedidoAndIdProducto(Long idPedido, Long idProducto) {
        String sql = "DELETE FROM detalle_pedido WHERE id_pedido = ? AND id_producto = ?";
        int rowsAffected = jdbcTemplate.update(sql, idPedido, idProducto);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteByPedidoId(Long idPedido) {
        String sql = "DELETE FROM detalle_pedido WHERE id_pedido = ?";
        int rowsAffected = jdbcTemplate.update(sql, idPedido);
        return rowsAffected > 0;
    }

}
