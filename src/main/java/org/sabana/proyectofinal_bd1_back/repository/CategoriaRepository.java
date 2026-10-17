package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad Categoria.
 * Todas las operaciones se realizan exclusivamente mediante SQL nativo.
 */
public interface CategoriaRepository {

    Optional<Categoria> findById(Long id);

    List<Categoria> findAll();

    List<Categoria> findAllActivas();

    Categoria save(Categoria categoria);

    Categoria update(Categoria categoria);

    boolean deleteById(Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndNotId(String nombre, Long id);

}
