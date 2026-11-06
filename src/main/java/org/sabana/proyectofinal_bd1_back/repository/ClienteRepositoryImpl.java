package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepositoryImpl implements ClienteRepository {

    private static final String BASE_SELECT = """
            SELECT c.id_cliente,
                   c.nombre,
                   c.apellido,
                   (SELECT cc.correo FROM cliente_correo cc WHERE cc.cliente_id_cliente = c.id_cliente ORDER BY cc.correo LIMIT 1) AS correo,
                   (SELECT ct.telefono FROM cliente_telefono ct WHERE ct.cliente_id_cliente = c.id_cliente ORDER BY ct.telefono LIMIT 1) AS telefono
            FROM cliente c
            """;

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
        String sql = BASE_SELECT + " WHERE c.id_cliente = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, id);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Cliente> findAll() {
        String sql = BASE_SELECT + " ORDER BY c.id_cliente ASC";
        return jdbcTemplate.query(sql, clienteRowMapper);
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        String sqlCliente = "INSERT INTO cliente (nombre, apellido) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlCliente, new String[]{"id_cliente"});
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido() != null ? cliente.getApellido() : "");
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            cliente.setIdCliente(generatedKey.longValue());
        }

        if (cliente.getIdCliente() != null) {
            if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
                jdbcTemplate.update(
                        "INSERT INTO cliente_correo (correo, cliente_id_cliente) VALUES (?, ?)",
                        cliente.getCorreo().trim(),
                        cliente.getIdCliente()
                );
            }
            if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
                jdbcTemplate.update(
                        "INSERT INTO cliente_telefono (telefono, cliente_id_cliente) VALUES (?, ?)",
                        cliente.getTelefono().trim(),
                        cliente.getIdCliente()
                );
            }
        }
        return cliente;
    }

    @Override
    @Transactional
    public Cliente update(Cliente cliente) {
        String sqlUpdateCliente = "UPDATE cliente SET nombre = ?, apellido = ? WHERE id_cliente = ?";
        jdbcTemplate.update(
                sqlUpdateCliente,
                cliente.getNombre(),
                cliente.getApellido() != null ? cliente.getApellido() : "",
                cliente.getIdCliente()
        );

        jdbcTemplate.update("DELETE FROM cliente_correo WHERE cliente_id_cliente = ?", cliente.getIdCliente());
        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            jdbcTemplate.update(
                    "INSERT INTO cliente_correo (correo, cliente_id_cliente) VALUES (?, ?)",
                    cliente.getCorreo().trim(),
                    cliente.getIdCliente()
            );
        }

        jdbcTemplate.update("DELETE FROM cliente_telefono WHERE cliente_id_cliente = ?", cliente.getIdCliente());
        if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
            jdbcTemplate.update(
                    "INSERT INTO cliente_telefono (telefono, cliente_id_cliente) VALUES (?, ?)",
                    cliente.getTelefono().trim(),
                    cliente.getIdCliente()
            );
        }

        return cliente;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM cliente_correo WHERE cliente_id_cliente = ?", id);
        jdbcTemplate.update("DELETE FROM cliente_telefono WHERE cliente_id_cliente = ?", id);
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM cliente_correo WHERE LOWER(TRIM(correo)) = LOWER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM cliente_telefono WHERE telefono = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCorreoAndNotId(String correo, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente_correo WHERE LOWER(TRIM(correo)) = LOWER(TRIM(?)) AND cliente_id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefonoAndNotId(String telefono, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente_telefono WHERE telefono = ? AND cliente_id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Cliente> findByCorreo(String correo) {
        String sql = BASE_SELECT + " WHERE EXISTS (SELECT 1 FROM cliente_correo cc WHERE cc.cliente_id_cliente = c.id_cliente AND LOWER(TRIM(cc.correo)) = LOWER(TRIM(?)))";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, correo);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> findByTelefono(String telefono) {
        String sql = BASE_SELECT + " WHERE EXISTS (SELECT 1 FROM cliente_telefono ct WHERE ct.cliente_id_cliente = c.id_cliente AND ct.telefono = ?)";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, telefono);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasActiveReservas(Long idCliente) {
        String sql = "SELECT COUNT(*) FROM reserva WHERE cliente_id_cliente = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idCliente);
        return count != null && count > 0;
    }

    @Override
    public boolean hasPedidosOrFacturas(Long idCliente) {
        String sqlPedidos = "SELECT COUNT(*) FROM pedido WHERE cliente_id_cliente = ?";
        Integer countPedidos = jdbcTemplate.queryForObject(sqlPedidos, Integer.class, idCliente);
        return countPedidos != null && countPedidos > 0;
    }

    @Override
    public int deleteHistoricalReservasByClienteId(Long idCliente) {
        String sql = "DELETE FROM reserva WHERE cliente_id_cliente = ? AND estado IN ('CANCELADA', 'COMPLETADA')";
        return jdbcTemplate.update(sql, idCliente);
    }

}
