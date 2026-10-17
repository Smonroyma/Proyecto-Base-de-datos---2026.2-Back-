package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UsuarioRepository utilizando consultas SQL nativas y JdbcTemplate.
 */
@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Usuario> usuarioRowMapper = (rs, rowNum) -> Usuario.builder()
            .idUsuario(rs.getLong("id_usuario"))
            .nombre(rs.getString("nombre"))
            .email(rs.getString("email"))
            .passwordHash(rs.getString("password_hash"))
            .rol(rs.getString("rol"))
            .activo(rs.getBoolean("activo"))
            .dosFactoresHabilitado(rs.getBoolean("dos_factores_habilitado"))
            .codigoOtp(rs.getString("codigo_otp"))
            .expiracionOtp(rs.getTimestamp("expiracion_otp") != null 
                    ? rs.getTimestamp("expiracion_otp").toLocalDateTime() 
                    : null)
            .fechaRegistro(rs.getTimestamp("fecha_registro") != null 
                    ? rs.getTimestamp("fecha_registro").toLocalDateTime() 
                    : null)
            .build();

    public UsuarioRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT id_usuario, nombre, email, password_hash, rol, activo, dos_factores_habilitado, " +
                "codigo_otp, expiracion_otp, fecha_registro FROM usuario WHERE id_usuario = ?";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(sql, usuarioRowMapper, id);
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id_usuario, nombre, email, password_hash, rol, activo, dos_factores_habilitado, " +
                "codigo_otp, expiracion_otp, fecha_registro FROM usuario WHERE LOWER(email) = LOWER(?)";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(sql, usuarioRowMapper, email);
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT id_usuario, nombre, email, password_hash, rol, activo, dos_factores_habilitado, " +
                "codigo_otp, expiracion_otp, fecha_registro FROM usuario ORDER BY id_usuario ASC";
        return jdbcTemplate.query(sql, usuarioRowMapper);
    }

    @Override
    public Usuario save(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, email, password_hash, rol, activo, dos_factores_habilitado, " +
                "codigo_otp, expiracion_otp, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime fecha = usuario.getFechaRegistro() != null 
                ? usuario.getFechaRegistro() 
                : LocalDateTime.now();
        boolean activo = usuario.getActivo() != null ? usuario.getActivo() : true;
        boolean dosFactores = usuario.getDosFactoresHabilitado() != null ? usuario.getDosFactoresHabilitado() : false;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id_usuario" });
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail().toLowerCase());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getRol().toUpperCase());
            ps.setBoolean(5, activo);
            ps.setBoolean(6, dosFactores);
            ps.setString(7, usuario.getCodigoOtp());
            ps.setTimestamp(8, usuario.getExpiracionOtp() != null ? Timestamp.valueOf(usuario.getExpiracionOtp()) : null);
            ps.setTimestamp(9, Timestamp.valueOf(fecha));
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            usuario.setIdUsuario(generatedKey.longValue());
        }
        usuario.setActivo(activo);
        usuario.setDosFactoresHabilitado(dosFactores);
        usuario.setFechaRegistro(fecha);
        usuario.setEmail(usuario.getEmail().toLowerCase());
        usuario.setRol(usuario.getRol().toUpperCase());
        return usuario;
    }

    @Override
    public Usuario update(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, rol = ?, activo = ?, " +
                "dos_factores_habilitado = ? WHERE id_usuario = ?";
        jdbcTemplate.update(
                sql,
                usuario.getNombre(),
                usuario.getEmail().toLowerCase(),
                usuario.getRol().toUpperCase(),
                usuario.getActivo(),
                usuario.getDosFactoresHabilitado(),
                usuario.getIdUsuario()
        );
        return usuario;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailAndNotId(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?) AND id_usuario <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    @Override
    public void guardarCodigoOTP(Long idUsuario, String codigoOtp, LocalDateTime expiracion) {
        String sql = "UPDATE usuario SET codigo_otp = ?, expiracion_otp = ? WHERE id_usuario = ?";
        jdbcTemplate.update(sql, codigoOtp, Timestamp.valueOf(expiracion), idUsuario);
    }

    @Override
    public void limpiarCodigoOTP(Long idUsuario) {
        String sql = "UPDATE usuario SET codigo_otp = NULL, expiracion_otp = NULL WHERE id_usuario = ?";
        jdbcTemplate.update(sql, idUsuario);
    }

}
