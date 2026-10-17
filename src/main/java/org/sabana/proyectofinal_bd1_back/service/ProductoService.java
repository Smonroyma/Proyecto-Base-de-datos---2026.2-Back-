package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.ProductoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoResponseDTO;

import java.util.List;

/**
 * Contrato de servicio para la gestión de productos en Savana Data Coffee.
 */
public interface ProductoService {

    List<ProductoResponseDTO> obtenerProductos(Long categoriaId, Boolean disponible);

    ProductoResponseDTO buscarPorId(Long id);

    ProductoResponseDTO crear(ProductoRequestDTO requestDTO);

    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO requestDTO);

    void eliminar(Long id);

}
