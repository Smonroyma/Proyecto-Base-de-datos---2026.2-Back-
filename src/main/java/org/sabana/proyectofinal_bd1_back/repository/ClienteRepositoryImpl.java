package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ClienteRepository utilizando exclusivamente sentencias SQL nativas y JdbcTemplate.
 */
@Repository
public class ClienteRepositoryImpl implements ClienteRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Cliente> clienteRowMapper = (rs, rowNum) -> Cliente.builder()
            .idCliente(rs.getLong("id_cliente"))
            .nombre(rs.getString("nombre"))
            .apellido(rs.getString("apellido"))
            .correo(rs.getString("correo"))
            .telefono(rs.getString("telefono"))
            .build();

    public ClienteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        String sql = "SELECT id_cliente, nombre, apellido, correo, telefono FROM cliente WHERE id_cliente = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, id);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Cliente> findAll() {
        String sql = "SELECT id_cliente, nombre, apellido, correo, telefono FROM cliente ORDER BY id_cliente ASC";
        return jdbcTemplate.query(sql, clienteRowMapper);
    }

    @Override
    public Cliente save(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, apellido, correo, telefono) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_cliente"});
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido() != null ? cliente.getApellido() : "");
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getTelefono());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            cliente.setIdCliente(generatedKey.longValue());
        }
        return cliente;
    }

    @Override
    public Cliente update(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre = ?, apellido = ?, correo = ?, telefono = ? WHERE id_cliente = ?";
        jdbcTemplate.update(
                sql,
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCorreo(),
                cliente.getTelefono(),
                cliente.getIdCliente()
        );
        return cliente;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE LOWER(correo) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE telefono = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCorreoAndNotId(String correo, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE LOWER(correo) = LOWER(?) AND id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefonoAndNotId(String telefono, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE telefono = ? AND id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Cliente> findByCorreo(String correo) {
        String sql = "SELECT id_cliente, nombre, apellido, correo, telefono FROM cliente WHERE LOWER(correo) = LOWER(?)";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, correo);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> findByTelefono(String telefono) {
        String sql = "SELECT id_cliente, nombre, apellido, correo, telefono FROM cliente WHERE telefono = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, telefono);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasActiveReservas(Long idCliente) {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_cliente = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idCliente);
        return count != null && count > 0;
    }

    @Override
    public boolean hasPedidosOrFacturas(Long idCliente) {
        String sqlPedidos = "SELECT COUNT(*) FROM pedido WHERE id_cliente = ?";
        Integer countPedidos = jdbcTemplate.queryForObject(sqlPedidos, Integer.class, idCliente);
        return countPedidos != null && countPedidos > 0;
    }

    @Override
    public int deleteHistoricalReservasByClienteId(Long idCliente) {
        String sql = "DELETE FROM reserva WHERE id_cliente = ? AND estado IN ('CANCELADA', 'COMPLETADA')";
        return jdbcTemplate.update(sql, idCliente);
    }

}
