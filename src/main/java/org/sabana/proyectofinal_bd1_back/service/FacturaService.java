package org.sabana.proyectofinal_bd1_back.service;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de servicio para el procesamiento de pagos (Pago -> Factura con 19% IVA) y consulta de facturas/pagos.
 */
public interface FacturaService {

    FacturaResponseDTO procesarPagoYEmitirFactura(FacturaRequestDTO requestDTO);

    FacturaResponseDTO buscarPorNumFactura(String numFactura);

    FacturaResponseDTO buscarPorPedidoId(Long idPedido);

    List<FacturaResponseDTO> obtenerFacturas(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<PagoResponseDTO> obtenerPagos(String numFactura);

}
