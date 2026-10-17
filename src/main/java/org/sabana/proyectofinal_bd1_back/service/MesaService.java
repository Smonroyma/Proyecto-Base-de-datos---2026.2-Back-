package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para la lógica de negocio del módulo de Mesas.
 */
public interface MesaService {

    MesaResponseDTO buscarPorId(Long id);

    List<MesaResponseDTO> obtenerMesas(String estado);

    MesaResponseDTO buscarPorNumero(Integer numero);

    MesaResponseDTO crear(MesaRequestDTO requestDTO);

    MesaResponseDTO actualizar(Long id, MesaRequestDTO requestDTO);

    void eliminar(Long id);

}
