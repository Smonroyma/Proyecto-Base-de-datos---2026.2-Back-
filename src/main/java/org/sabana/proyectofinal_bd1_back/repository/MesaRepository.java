package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Mesa;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de persistencia de Mesa mediante SQL nativo.
 */
public interface MesaRepository {

    Optional<Mesa> findByNumeroMesa(Integer numeroMesa);

    List<Mesa> findAll();

    List<Mesa> findByEstado(String estado);

    Mesa save(Mesa mesa);

    Mesa update(Mesa mesa);

    boolean updateEstado(Integer numeroMesa, String estado);

    boolean deleteByNumeroMesa(Integer numeroMesa);

    boolean existsByNumeroMesa(Integer numeroMesa);

    long countByEstado(String estado);

}
