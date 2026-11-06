package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;

import java.util.List;

public interface MetodoPagoService {

    List<MetodoPagoResponseDTO> obtenerMetodosPago(Boolean soloActivos);

    MetodoPagoResponseDTO buscarPorTipo(String tipo);

    MetodoPagoResponseDTO crear(MetodoPagoRequestDTO requestDTO);

    MetodoPagoResponseDTO actualizar(String tipo, MetodoPagoRequestDTO requestDTO);

    void eliminar(String tipo);

}
