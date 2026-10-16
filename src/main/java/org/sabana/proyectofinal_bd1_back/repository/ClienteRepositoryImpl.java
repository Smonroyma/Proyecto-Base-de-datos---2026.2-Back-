package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
            .telefono(rs.getString("telefono"))
            .email(rs.getString("email"))
            .fechaRegistro(rs.getTimestamp("fecha_registro") != null 
                    ? rs.getTimestamp("fecha_registro").toLocalDateTime() 
                    : null)
            .build();

    public ClienteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        String sql = "SELECT id_cliente, nombre, telefono, email, fecha_registro FROM cliente WHERE id_cliente = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, id);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Cliente> findAll() {
        String sql = "SELECT id_cliente, nombre, telefono, email, fecha_registro FROM cliente ORDER BY id_cliente ASC";
        return jdbcTemplate.query(sql, clienteRowMapper);
    }

    @Override
    public Cliente save(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, telefono, email, fecha_registro) VALUES (?, ?, ?, ?)";
        LocalDateTime fechaRegistro = cliente.getFechaRegistro() != null 
                ? cliente.getFechaRegistro() 
                : LocalDateTime.now();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_cliente" });
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getEmail());
            ps.setTimestamp(4, Timestamp.valueOf(fechaRegistro));
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            cliente.setIdCliente(generatedKey.longValue());
        }
        cliente.setFechaRegistro(fechaRegistro);
        return cliente;
    }

    @Override
    public Cliente update(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre = ?, telefono = ?, email = ? WHERE id_cliente = ?";
        jdbcTemplate.update(
                sql,
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getEmail(),
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
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE telefono = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailAndNotId(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE LOWER(email) = LOWER(?) AND id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelefonoAndNotId(String telefono, Long id) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE telefono = ? AND id_cliente <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, telefono, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Cliente> findByEmail(String email) {
        String sql = "SELECT id_cliente, nombre, telefono, email, fecha_registro FROM cliente WHERE LOWER(email) = LOWER(?)";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, email);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> findByTelefono(String telefono) {
        String sql = "SELECT id_cliente, nombre, telefono, email, fecha_registro FROM cliente WHERE telefono = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, clienteRowMapper, telefono);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
