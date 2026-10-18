package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad MetodoPago.
 */
public interface MetodoPagoRepository {

    Optional<MetodoPago> findById(Long id);

    List<MetodoPago> findAll();

    List<MetodoPago> findAllActivos();

    Optional<MetodoPago> findByCodigo(String codigo);

    MetodoPago save(MetodoPago metodoPago);

    MetodoPago update(MetodoPago metodoPago);

    boolean deleteById(Long id);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndNotId(String codigo, Long id);

}
