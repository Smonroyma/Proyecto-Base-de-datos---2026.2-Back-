package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Administrador;
import org.sabana.proyectofinal_bd1_back.model.entity.Mesero;
import org.sabana.proyectofinal_bd1_back.model.entity.Staff;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UsuarioRepository utilizando consultas SQL nativas y JdbcTemplate
 * realizando JOIN entre la tabla padre 'staff' y las tablas hijas 'administrador' y 'mesero'.
 */
@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = """
            SELECT s.id_usuario, s.nombres, s.apellidos, s.correo, s.contrasena,
                   s.dos_fac_hab, s.fecha_inicio,
                   CASE WHEN a.id_usuario IS NOT NULL THEN 'ADMINISTRADOR' ELSE 'MESERO' END AS rol,
                   m.estado AS estado_mesero
            FROM staff s
            LEFT JOIN administrador a ON s.id_usuario = a.id_usuario
            LEFT JOIN mesero m ON s.id_usuario = m.id_usuario
            """;

    private final RowMapper<Staff> staffRowMapper = (rs, rowNum) -> {
        Long idUsuario = rs.getLong("id_usuario");
        String nombres = rs.getString("nombres");
        String apellidos = rs.getString("apellidos");
        String correo = rs.getString("correo");
        String contrasena = rs.getString("contrasena");
        boolean dosFacHab = rs.getBoolean("dos_fac_hab");
        Date sqlDate = rs.getDate("fecha_inicio");
        LocalDate fechaInicio = sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now();
        String rol = rs.getString("rol");

        if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            return Administrador.builder()
                    .idUsuario(idUsuario)
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .correo(correo)
                    .contrasena(contrasena)
                    .dosFacHab(dosFacHab)
                    .fechaInicio(fechaInicio)
                    .rol("ADMINISTRADOR")
                    .build();
        } else {
            String estado = rs.getString("estado_mesero");
            return Mesero.builder()
                    .idUsuario(idUsuario)
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .correo(correo)
                    .contrasena(contrasena)
                    .dosFacHab(dosFacHab)
                    .fechaInicio(fechaInicio)
                    .rol("MESERO")
                    .estado(estado != null ? estado : "ACTIVO")
                    .build();
        }
    };

    public UsuarioRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Staff> findById(Long id) {
        String sql = BASE_SELECT + " WHERE s.id_usuario = ?";
        try {
            Staff staff = jdbcTemplate.queryForObject(sql, staffRowMapper, id);
            return Optional.ofNullable(staff);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Staff> findByCorreo(String correo) {
        String sql = BASE_SELECT + " WHERE LOWER(TRIM(s.correo)) = LOWER(TRIM(?))";
        try {
            Staff staff = jdbcTemplate.queryForObject(sql, staffRowMapper, correo);
            return Optional.ofNullable(staff);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Staff> findAll() {
        String sql = BASE_SELECT + " ORDER BY s.id_usuario ASC";
        return jdbcTemplate.query(sql, staffRowMapper);
    }

    @Override
    @Transactional
    public Staff save(Staff staff) {
        String sqlStaff = """
                INSERT INTO staff (nombres, apellidos, correo, contrasena, dos_fac_hab, fecha_inicio)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        LocalDate fechaInicio = staff.getFechaInicio() != null ? staff.getFechaInicio() : LocalDate.now();
        boolean dosFac = Boolean.TRUE.equals(staff.getDosFacHab());
        String correoLower = staff.getCorreo().trim().toLowerCase();
        String apellidos = staff.getApellidos() != null ? staff.getApellidos().trim() : "";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlStaff, new String[]{"id_usuario"});
            ps.setString(1, staff.getNombres().trim());
            ps.setString(2, apellidos);
            ps.setString(3, correoLower);
            ps.setString(4, staff.getContrasena());
            ps.setBoolean(5, dosFac);
            ps.setDate(6, Date.valueOf(fechaInicio));
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            staff.setIdUsuario(generatedKey.longValue());
        }

        String rol = staff.getRol() != null ? staff.getRol().trim().toUpperCase()
                : (staff instanceof Administrador ? "ADMINISTRADOR" : "MESERO");

        if ("ADMINISTRADOR".equals(rol)) {
            jdbcTemplate.update("INSERT INTO administrador (id_usuario) VALUES (?)", staff.getIdUsuario());
            staff.setRol("ADMINISTRADOR");
        } else {
            String estado = "ACTIVO";
            if (staff instanceof Mesero mesero && mesero.getEstado() != null && !mesero.getEstado().isBlank()) {
                estado = mesero.getEstado().trim().toUpperCase();
                mesero.setEstado(estado);
            }
            jdbcTemplate.update("INSERT INTO mesero (id_usuario, estado) VALUES (?, ?)", staff.getIdUsuario(), estado);
            staff.setRol("MESERO");
        }

        staff.setCorreo(correoLower);
        staff.setApellidos(apellidos);
        staff.setDosFacHab(dosFac);
        staff.setFechaInicio(fechaInicio);
        return staff;
    }

    @Override
    @Transactional
    public Staff update(Staff staff) {
        String correoLower = staff.getCorreo().trim().toLowerCase();
        String apellidos = staff.getApellidos() != null ? staff.getApellidos().trim() : "";
        boolean dosFac = Boolean.TRUE.equals(staff.getDosFacHab());

        String sqlUpdateStaff = """
                UPDATE staff
                SET nombres = ?, apellidos = ?, correo = ?, contrasena = ?, dos_fac_hab = ?
                WHERE id_usuario = ?
                """;
        jdbcTemplate.update(
                sqlUpdateStaff,
                staff.getNombres().trim(),
                apellidos,
                correoLower,
                staff.getContrasena(),
                dosFac,
                staff.getIdUsuario()
        );

        String rol = staff.getRol() != null ? staff.getRol().trim().toUpperCase()
                : (staff instanceof Administrador ? "ADMINISTRADOR" : "MESERO");

        if ("ADMINISTRADOR".equals(rol)) {
            jdbcTemplate.update("DELETE FROM mesero WHERE id_usuario = ?", staff.getIdUsuario());
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM administrador WHERE id_usuario = ?", Integer.class, staff.getIdUsuario());
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO administrador (id_usuario) VALUES (?)", staff.getIdUsuario());
            }
            staff.setRol("ADMINISTRADOR");
        } else {
            jdbcTemplate.update("DELETE FROM administrador WHERE id_usuario = ?", staff.getIdUsuario());
            String estado = "ACTIVO";
            if (staff instanceof Mesero mesero && mesero.getEstado() != null && !mesero.getEstado().isBlank()) {
                estado = mesero.getEstado().trim().toUpperCase();
                mesero.setEstado(estado);
            }
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mesero WHERE id_usuario = ?", Integer.class, staff.getIdUsuario());
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO mesero (id_usuario, estado) VALUES (?, ?)", staff.getIdUsuario(), estado);
            } else {
                jdbcTemplate.update("UPDATE mesero SET estado = ? WHERE id_usuario = ?", estado, staff.getIdUsuario());
            }
            staff.setRol("MESERO");
        }

        staff.setCorreo(correoLower);
        staff.setApellidos(apellidos);
        staff.setDosFacHab(dosFac);
        return staff;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM administrador WHERE id_usuario = ?", id);
        jdbcTemplate.update("DELETE FROM mesero WHERE id_usuario = ?", id);
        int rows = jdbcTemplate.update("DELETE FROM staff WHERE id_usuario = ?", id);
        return rows > 0;
    }

    @Override
    public boolean existsByCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM staff WHERE LOWER(TRIM(correo)) = LOWER(TRIM(?))";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCorreoAndNotId(String correo, Long id) {
        String sql = "SELECT COUNT(*) FROM staff WHERE LOWER(TRIM(correo)) = LOWER(TRIM(?)) AND id_usuario <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, correo, id);
        return count != null && count > 0;
    }

}
