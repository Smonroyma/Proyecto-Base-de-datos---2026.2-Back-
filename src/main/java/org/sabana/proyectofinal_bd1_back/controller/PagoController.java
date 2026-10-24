package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.FacturaService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el procesamiento de pagos y generación automática de facturas (IVA 19%) en Savana Data Coffee.
 * Regla V2: Exclusivo para ROLE_MESERO en el registro de pagos (POST /api/pagos).
 */
@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Endpoints para procesar el pago de pedidos en mesa y generar automáticamente su factura con 19% IVA")
public class PagoController {

    private final FacturaService facturaService;

    public PagoController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping
    @Operation(
            summary = "Procesar pago de pedido y generar factura (IVA 19%)",
            description = "El mesero registra el pago seleccionando los métodos de pago (EFECTIVO, TARJETA, TRANSFERENCIA). El sistema calcula el IVA del 19%, valida el cuadre exacto, genera automáticamente la FACTURA en estado PAGADA, pasa el PEDIDO a PAGADO, libera la MESA a DISPONIBLE y envía la factura por correo HTML."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago procesado y factura generada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacturaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido, mesero, cliente o método de pago no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "El pedido ya fue facturado previamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Descuadre en el monto del pago o pedido cancelado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<FacturaResponseDTO> registrarPago(
            @Valid @RequestBody FacturaRequestDTO requestDTO) {
        FacturaResponseDTO facturaGenerada = facturaService.procesarPagoYEmitirFactura(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaGenerada);
    }

    @GetMapping
    @Operation(
            summary = "Listar pagos procesados",
            description = "Obtiene todos los pagos registrados o filtrados por número de factura."
    )
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagos(
            @Parameter(description = "Número de factura opcional para filtrar pagos", example = "FAC-2026-00001")
            @RequestParam(name = "numFactura", required = false) String numFactura) {
        return ResponseEntity.ok(facturaService.obtenerPagos(numFactura));
    }

    @GetMapping("/factura/{numFactura}")
    @Operation(
            summary = "Listar pagos por número de factura",
            description = "Obtiene el desglose de pagos asociados a una factura específica."
    )
    public ResponseEntity<List<PagoResponseDTO>> obtenerPagosPorFactura(
            @Parameter(description = "Número consecutivo de la factura", example = "FAC-2026-00001", required = true)
            @PathVariable String numFactura) {
        return ResponseEntity.ok(facturaService.obtenerPagos(numFactura));
    }

}
