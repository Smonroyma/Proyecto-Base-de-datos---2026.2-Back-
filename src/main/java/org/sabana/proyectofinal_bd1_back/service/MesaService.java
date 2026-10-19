package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para la lógica de negocio del módulo de Mesas.
 */
public interface MesaService {

    MesaResponseDTO buscarPorNumero(Integer numeroMesa);

    List<MesaResponseDTO> obtenerMesas(String estado);

    MesaResponseDTO crear(MesaRequestDTO requestDTO);

    MesaResponseDTO actualizar(Integer numeroMesa, MesaRequestDTO requestDTO);

    MesaResponseDTO actualizarEstado(Integer numeroMesa, String nuevoEstado);

    void eliminar(Integer numeroMesa);

}
