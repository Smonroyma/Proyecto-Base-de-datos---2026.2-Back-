package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de persistencia de Mesa mediante SQL nativo.
 */
public interface MesaRepository {

    Optional<Mesa> findById(Long id);

    List<Mesa> findAll();

    List<Mesa> findByEstado(String estado);

    Optional<Mesa> findByNumero(Integer numero);

    Mesa save(Mesa mesa);

    Mesa update(Mesa mesa);

    boolean deleteById(Long id);

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndNotId(Integer numero, Long id);

    long countByEstado(String estado);

}
