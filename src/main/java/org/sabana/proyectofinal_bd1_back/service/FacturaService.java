package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de servicio para la gestión de facturación y procesamiento de pagos.
 */
public interface FacturaService {

    FacturaResponseDTO emitirFactura(FacturaRequestDTO requestDTO);

    FacturaResponseDTO buscarPorId(Long id);

    FacturaResponseDTO buscarPorPedidoId(Long idPedido);

    List<FacturaResponseDTO> obtenerFacturas(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    FacturaResponseDTO anularFactura(Long id);

}
