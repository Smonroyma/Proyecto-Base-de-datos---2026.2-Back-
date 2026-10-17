package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.PedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoResponseDTO;

import java.util.List;

/**
 * Contrato de servicio para la gestión de pedidos y comandas en Savana Data Coffee.
 */
public interface PedidoService {

    PedidoResponseDTO crearPedido(PedidoRequestDTO requestDTO);

    PedidoResponseDTO buscarPorId(Long id);

    List<PedidoResponseDTO> obtenerPedidos(Long clienteId, String estado);

    PedidoResponseDTO actualizarEstado(Long id, String nuevoEstado);

    void eliminarPedido(Long id);

}
