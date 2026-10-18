package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;

import java.util.List;

/**
 * Contrato de servicio para la gestión de métodos de pago.
 */
public interface MetodoPagoService {

    List<MetodoPagoResponseDTO> obtenerMetodosPago(Boolean soloActivos);

    MetodoPagoResponseDTO buscarPorId(Long id);

    MetodoPagoResponseDTO buscarPorCodigo(String codigo);

    MetodoPagoResponseDTO crear(MetodoPagoRequestDTO requestDTO);

    MetodoPagoResponseDTO actualizar(Long id, MetodoPagoRequestDTO requestDTO);

    void eliminar(Long id);

}
