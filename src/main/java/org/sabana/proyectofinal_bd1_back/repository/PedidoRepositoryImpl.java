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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepositoryImpl implements PedidoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DetallePedidoRepository detallePedidoRepository;

    private static final String BASE_SELECT = """
            SELECT p.id_pedido, p.cliente_id_cliente AS id_cliente,
                   CONCAT(c.nombre, ' ', c.apellido) AS nombre_cliente,
                   p.mesa_num_mesa AS numero_mesa,
                   p.staff_id_usuario AS id_usuario,
                   CONCAT(s.nombres, ' ', s.apellidos) AS nombre_usuario,
                   p.fecha, p.estado, p.observaciones
            FROM pedido p
            INNER JOIN cliente c ON p.cliente_id_cliente = c.id_cliente
            INNER JOIN mesa m ON p.mesa_num_mesa = m.num_mesa
            INNER JOIN staff s ON p.staff_id_usuario = s.id_usuario
            """;

    private final RowMapper<Pedido> pedidoRowMapper = (rs, rowNum) -> Pedido.builder()
            .idPedido(rs.getLong("id_pedido"))
            .idCliente(rs.getObject("id_cliente") != null ? rs.getLong("id_cliente") : null)
            .nombreCliente(rs.getString("nombre_cliente") != null ? rs.getString("nombre_cliente").trim() : null)
            .numeroMesa(rs.getObject("numero_mesa") != null ? rs.getInt("numero_mesa") : null)
            .idUsuario(rs.getObject("id_usuario") != null ? rs.getLong("id_usuario") : null)
            .nombreUsuario(rs.getString("nombre_usuario") != null ? rs.getString("nombre_usuario").trim() : null)
            .fecha(rs.getTimestamp("fecha") != null ? rs.getTimestamp("fecha").toLocalDateTime() : null)
            .estado(rs.getString("estado"))
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
        String sql = BASE_SELECT + " ORDER BY p.fecha DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByCliente(Long idCliente) {
        String sql = BASE_SELECT + " WHERE p.cliente_id_cliente = ? ORDER BY p.fecha DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, idCliente);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByMesa(Integer numeroMesa) {
        String sql = BASE_SELECT + " WHERE p.mesa_num_mesa = ? ORDER BY p.fecha DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, numeroMesa);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByUsuario(Long idUsuario) {
        String sql = BASE_SELECT + " WHERE p.staff_id_usuario = ? ORDER BY p.fecha DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, idUsuario);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public List<Pedido> findByEstado(String estado) {
        String sql = BASE_SELECT + " WHERE UPPER(p.estado) = UPPER(?) ORDER BY p.fecha DESC, p.id_pedido DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, pedidoRowMapper, estado);
        cargarDetallesEnLista(pedidos);
        return pedidos;
    }

    @Override
    public Pedido save(Pedido pedido) {
        String sql = """
                INSERT INTO pedido (cliente_id_cliente, mesa_num_mesa, staff_id_usuario, fecha, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime fecha = pedido.getFecha() != null ? pedido.getFecha() : LocalDateTime.now();
        String estadoUpper = pedido.getEstado() != null ? pedido.getEstado().toUpperCase().trim() : "PENDIENTE";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_pedido"});
            ps.setLong(1, pedido.getIdCliente());
            ps.setInt(2, pedido.getNumeroMesa());
            ps.setLong(3, pedido.getIdUsuario());
            ps.setTimestamp(4, Timestamp.valueOf(fecha));
            ps.setString(5, estadoUpper);
            ps.setString(6, pedido.getObservaciones());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            pedido.setIdPedido(generatedKey.longValue());
        }
        pedido.setFecha(fecha);
        pedido.setEstado(estadoUpper);
        return pedido;
    }

    @Override
    public boolean updateEstado(Long idPedido, String nuevoEstado) {
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
        int rowsAffected = jdbcTemplate.update(sql, nuevoEstado.toUpperCase().trim(), idPedido);
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
