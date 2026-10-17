package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de persistencia de Usuario mediante SQL nativo.
 */
public interface UsuarioRepository {

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findAll();

    Usuario save(Usuario usuario);

    Usuario update(Usuario usuario);

    boolean deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndNotId(String email, Long id);

    void guardarCodigoOTP(Long idUsuario, String codigoOtp, LocalDateTime expiracion);

    void limpiarCodigoOTP(Long idUsuario);

}
