package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.FacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST de solo lectura para la consulta de facturas en Savana Data Coffee.
 * Regla V2: Las facturas se generan automáticamente al registrar el pago en POST /api/pagos.
 */
@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Endpoints de consulta de comprobantes y facturas electrónicas (IVA 19%)")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    @Operation(
            summary = "Consultar facturas",
            description = "Obtiene el listado de facturas con sus pagos detallados. Permite filtrar opcionalmente por cliente y rango de fechas."
    )
    public ResponseEntity<List<FacturaResponseDTO>> obtenerFacturas(
            @Parameter(description = "Identificador opcional del cliente para filtrar", example = "1")
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @Parameter(description = "Fecha y hora inicial del filtro (ISO-8601)", example = "2026-10-01T00:00:00")
            @RequestParam(name = "fechaInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha y hora final del filtro (ISO-8601)", example = "2026-10-31T23:59:59")
            @RequestParam(name = "fechaFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<FacturaResponseDTO> facturas = facturaService.obtenerFacturas(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{numFactura}")
    @Operation(
            summary = "Consultar factura por número de factura",
            description = "Retorna el comprobante completo de una factura (ej. FAC-2026-00001) junto con el desglose de todos sus pagos asociados."
    )
    public ResponseEntity<FacturaResponseDTO> buscarPorNumFactura(
            @Parameter(description = "Número consecutivo único de la factura", example = "FAC-2026-00001", required = true)
            @PathVariable String numFactura) {
        FacturaResponseDTO factura = facturaService.buscarPorNumFactura(numFactura);
        return ResponseEntity.ok(factura);
    }

    @GetMapping("/pedido/{idPedido}")
    @Operation(
            summary = "Consultar factura por ID de pedido",
            description = "Retorna la factura única (1:1) asociada a un pedido específico."
    )
    public ResponseEntity<FacturaResponseDTO> buscarPorPedidoId(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long idPedido) {
        FacturaResponseDTO factura = facturaService.buscarPorPedidoId(idPedido);
        return ResponseEntity.ok(factura);
    }

}
