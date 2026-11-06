package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.MetodoPago;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository {

    Optional<MetodoPago> findByTipo(String tipo);

    List<MetodoPago> findAll();

    List<MetodoPago> findAllActivos();

    MetodoPago save(MetodoPago metodoPago);

    MetodoPago update(String tipoOriginal, MetodoPago metodoPago);

    boolean deleteByTipo(String tipo);

    boolean existsByTipo(String tipo);

}
