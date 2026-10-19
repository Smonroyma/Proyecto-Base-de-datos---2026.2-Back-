package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Staff;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de persistencia de la jerarquía Staff (Administrador y Empleado) mediante SQL nativo.
 * El código OTP 2FA no se almacena en base de datos, sino en memoria (ConcurrentHashMap).
 */
public interface UsuarioRepository {

    Optional<Staff> findById(Long id);

    Optional<Staff> findByCorreo(String correo);

    default Optional<Staff> findByEmail(String email) {
        return findByCorreo(email);
    }

    List<Staff> findAll();

    Staff save(Staff staff);

    Staff update(Staff staff);

    boolean deleteById(Long id);

    boolean existsByCorreo(String correo);

    default boolean existsByEmail(String email) {
        return existsByCorreo(email);
    }

    boolean existsByCorreoAndNotId(String correo, Long id);

    default boolean existsByEmailAndNotId(String email, Long id) {
        return existsByCorreoAndNotId(email, id);
    }

}
