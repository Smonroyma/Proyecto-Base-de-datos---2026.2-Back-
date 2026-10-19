package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Categoria;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad Categoria.
 * Todas las operaciones se realizan exclusivamente mediante SQL nativo.
 */
public interface CategoriaRepository {

    Optional<Categoria> findByNombreCategoria(String nombreCategoria);

    List<Categoria> findAll();

    List<Categoria> findByEstado(String estado);

    Categoria save(Categoria categoria);

    Categoria update(String nombreCategoriaOriginal, Categoria categoria);

    boolean deleteByNombreCategoria(String nombreCategoria);

    boolean existsByNombreCategoria(String nombreCategoria);

}
