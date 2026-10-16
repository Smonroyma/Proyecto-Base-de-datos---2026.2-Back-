package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de persistencia de Cliente mediante SQL nativo.
 */
public interface ClienteRepository {

    Optional<Cliente> findById(Long id);

    List<Cliente> findAll();

    Cliente save(Cliente cliente);

    Cliente update(Cliente cliente);

    boolean deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsByTelefono(String telefono);

    boolean existsByEmailAndNotId(String email, Long id);

    boolean existsByTelefonoAndNotId(String telefono, Long id);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByTelefono(String telefono);

}
