package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.ClienteRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO buscarPorId(Long id);

    List<ClienteResponseDTO> obtenerTodos();

    ClienteResponseDTO crear(ClienteRequestDTO requestDTO);

    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO requestDTO);

    void eliminar(Long id);

}
