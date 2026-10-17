package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para Pedido mediante SQL nativo y JdbcTemplate.
 */
@Repository
public class PedidoRepositoryImpl implements PedidoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DetallePedidoRepository detallePedidoRepository;

    private static final String BASE_SELECT = """
            SELECT p.id_pedido, p.id_cliente, c.nombre AS nombre_cliente,
                   p.id_mesa, m.numero AS numero_mesa,
                   p.id_usuario, u.nombre AS nombre_usuario,
                   p.fecha_hora, p.estado, p.tipo_pedido, p.total, p.observaciones
            FROM pedido p
            LEFT JOIN cliente c ON p.id_cliente = c.id_cliente
            LEFT JOIN mesa m ON p.id_mesa = m.id_mesa
            LEFT JOIN usuario u ON p.id_usuario = u.id_usuario
            """;

    private final RowMapper<Pedido> pedidoRowMapper = (rs, rowNum) -> Pedido.builder()
            .idPedido(rs.getLong("id_pedido"))
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente"))
            .idMesa(rs.getObject("id_mesa") != null ? rs.getLong("id_mesa") : null)
            .numeroMesa(rs.getObject("numero_mesa") != null ? rs.getInt("numero_mesa") : null)
            .idUsuario(rs.getObject("id_usuario") != null ? rs.getLong("id_usuario") : null)
            .nombreUsuario(rs.getString("nombre_usuario"))
            .fechaHora(rs.getTimestamp("fecha_hora") != null ? rs.getTimestamp("fecha_hora").toLocalDateTime() : null)
            .estado(rs.getString("estado"))
            .tipoPedido(rs.getString("tipo_pedido"))
            .total(rs.getObject("total") != null ? rs.getDouble("total") : 0.0)
            .observaciones(rs.getString("observaciones"))
            .detalles(new ArrayList<>())
            .build();

    public PedidoRepositoryImpl(JdbcTemplate jdbcTemplate, DetallePedidoRepository detallePedidoRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    public Optional<Pedido> findById(Long id) {
        String sql = BASE_SELECT + " WHERE p.id_pedido = ?";
        try {
            Pedido pedido = jdbcTemplate.queryForObject(sql, pedidoRowMapper, id);
            if (pedido != null) {
                List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(id);
                pedido.setDetalles(detalles);
            }
            return Optional.ofNullable(pedido);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Pedido> findAll() {
        String sql = BASE_SELECT + " ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByCliente(Long idCliente) {
        String sql = BASE_SELECT + " WHERE p.id_cliente = ? ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, idCliente);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByMesa(Long idMesa) {
        String sql = BASE_SELECT + " WHERE p.id_mesa = ? ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, idMesa);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByUsuario(Long idUsuario) {
        String sql = BASE_SELECT + " WHERE p.id_usuario = ? ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, idUsuario);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByEstado(String estado) {
        String sql = BASE_SELECT + " WHERE UPPER(p.estado) = UPPER(?) ORDER BY p.fecha_hora DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, estado);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public Pedido save(Pedido pedido) {
        String sql = """
                INSERT INTO pedido (id_cliente, id_mesa, id_usuario, fecha_hora, estado, tipo_pedido, total, observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime fecha = pedido.getFechaHora() != null ? pedido.getFechaHora() : LocalDateTime.now();
        String estadoUpper = pedido.getEstado() != null ? pedido.getEstado().toUpperCase() : "PENDIENTE";
        String tipoUpper = pedido.getTipoPedido() != null ? pedido.getTipoPedido().toUpperCase() : "MESA";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_pedido" });
            ps.setLong(1, pedido.getIdCliente());
            if (pedido.getIdMesa() != null) {
                ps.setLong(2, pedido.getIdMesa());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setLong(3, pedido.getIdUsuario());
            ps.setTimestamp(4, Timestamp.valueOf(fecha));
            ps.setString(5, estadoUpper);
            ps.setString(6, tipoUpper);
            ps.setDouble(7, pedido.getTotal() != null ? pedido.getTotal() : 0.0);
            ps.setString(8, pedido.getObservaciones());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            pedido.setIdPedido(generatedKey.longValue());
        }
        pedido.setFechaHora(fecha);
        pedido.setEstado(estadoUpper);
        pedido.setTipoPedido(tipoUpper);
        return pedido;
    }

    @Override
    public boolean updateEstado(Long idPedido, String nuevoEstado) {
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        int rowsAffected = jdbcTemplate.update(sql, nuevoEstado.toUpperCase(), idPedido);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        detallePedidoRepository.deleteByPedidoId(id);
        String sql = "DELETE FROM pedido WHERE id_pedido = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    private void cargarDetallesEnLista(List<Pedido> pedidos) {
        for (Pedido p : pedidos) {
            List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(p.getIdPedido());
            p.setDetalles(detalles);
        }
    }

}
