package org.sabana.proyectofinal_bd1_back.repository;

import org.sabana.proyectofinal_bd1_back.model.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    Optional<Producto> findById(Long id);

    List<Producto> findAll();

    List<Producto> findByCategoria(String nombreCategoria);

    List<Producto> findByDisponibilidad(Boolean disponibilidad);

    List<Producto> findByCategoriaAndDisponibilidad(String nombreCategoria, Boolean disponibilidad);

    Producto save(Producto producto);

    Producto update(Producto producto);

    boolean deleteById(Long id);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndNotId(String nombre, Long id);

    boolean updateStock(Long idProducto, Integer nuevoStock);

}
