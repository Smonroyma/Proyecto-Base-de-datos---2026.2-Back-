package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato del servicio de reservas en Savana Data Coffee.
 */
public interface ReservaService {

    List<ReservaResponseDTO> obtenerReservas(Long clienteId, Long mesaId, String estado);

    ReservaResponseDTO buscarPorId(Long id);

    ReservaResponseDTO crear(ReservaRequestDTO requestDTO);

    ReservaResponseDTO actualizar(Long id, ReservaRequestDTO requestDTO);

    ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado);

    void eliminar(Long id);

    List<MesaResponseDTO> consultarMesasDisponibles(LocalDateTime fechaHora, Integer numeroPersonas);

}
