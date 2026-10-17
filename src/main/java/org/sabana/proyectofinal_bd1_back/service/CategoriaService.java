package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaResponseDTO;

import java.util.List;

/**
 * Contrato de servicios de negocio para la gestión de categorías.
 */
public interface CategoriaService {

    List<CategoriaResponseDTO> obtenerCategorias(Boolean soloActivas);

    CategoriaResponseDTO buscarPorId(Long id);

    CategoriaResponseDTO crear(CategoriaRequestDTO requestDTO);

    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO requestDTO);

    void eliminar(Long id);

}
