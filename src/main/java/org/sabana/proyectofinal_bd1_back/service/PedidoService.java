package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.PedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoResponseDTO;

import java.util.List;

public interface PedidoService {

    PedidoResponseDTO crearPedido(PedidoRequestDTO requestDTO);

    PedidoResponseDTO buscarPorId(Long id);

    List<PedidoResponseDTO> obtenerPedidos(Long clienteId, String estado);

    PedidoResponseDTO actualizarEstado(Long id, String nuevoEstado);

    void eliminarPedido(Long id);

}
