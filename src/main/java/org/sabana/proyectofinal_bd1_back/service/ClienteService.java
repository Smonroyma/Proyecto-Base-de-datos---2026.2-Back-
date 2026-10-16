package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.ClienteRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio con la lógica de negocio para la gestión de clientes.
 */
public interface ClienteService {

    ClienteResponseDTO buscarPorId(Long id);

    List<ClienteResponseDTO> obtenerTodos();

    ClienteResponseDTO crear(ClienteRequestDTO requestDTO);

    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO requestDTO);

    void eliminar(Long id);

}
