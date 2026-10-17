package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la entidad Producto.
 * Todas las operaciones se realizan exclusivamente mediante SQL nativo.
 */
public interface ProductoRepository {

    Optional<Producto> findById(Long id);

    List<Producto> findAll();

    List<Producto> findByCategoria(Long idCategoria);

    List<Producto> findByDisponible(Boolean disponible);

    List<Producto> findByCategoriaAndDisponible(Long idCategoria, Boolean disponible);

    Producto save(Producto producto);

    Producto update(Producto producto);

    boolean deleteById(Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndNotId(String nombre, Long id);

}
