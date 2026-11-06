package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaService {

    List<ReservaResponseDTO> obtenerReservas(Long clienteId, String correo, Integer numeroMesa, String estado);

    List<ReservaResponseDTO> buscarPorCorreoCliente(String correo);

    ReservaResponseDTO buscarPorId(Long id);

    ReservaResponseDTO crear(ReservaRequestDTO requestDTO);

    ReservaResponseDTO actualizar(Long id, ReservaRequestDTO requestDTO);

    ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado);

    void eliminar(Long id);

    List<MesaResponseDTO> consultarMesasDisponibles(LocalDateTime fechaHora, Integer numeroPersonas);

}
