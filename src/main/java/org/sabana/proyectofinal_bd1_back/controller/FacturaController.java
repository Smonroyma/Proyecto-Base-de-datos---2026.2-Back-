package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.FacturaService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la emisión de facturas, procesamiento de pagos y cierre operativo en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Endpoints para la facturación, cobro en caja con múltiples medios de pago y anulación de comprobantes")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping
    @Operation(
            summary = "Emitir factura y procesar pagos",
            description = "Genera una factura fiscal para un pedido existente, valida el cuadre exacto de pagos (único o mixto), marca el pedido como PAGADO y libera la mesa a DISPONIBLE de forma transaccional."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura emitida y pagos procesados exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacturaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error de validación",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido, usuario, cliente o método de pago no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: el pedido ya fue facturado previamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Error de negocio en el pago: descuadre de montos, pedido cancelado o método de pago inactivo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<FacturaResponseDTO> emitirFactura(
            @Valid @RequestBody FacturaRequestDTO requestDTO) {
        FacturaResponseDTO facturaEmitida = facturaService.emitirFactura(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaEmitida);
    }

    @GetMapping
    @Operation(
            summary = "Consultar facturas",
            description = "Obtiene el listado de facturas con sus pagos detallados. Permite filtrar opcionalmente por cliente y rango de fechas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de facturas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FacturaResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
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

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar factura por ID",
            description = "Retorna el comprobante completo de una factura junto con el desglose de todos sus pagos asociados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura encontrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacturaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<FacturaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único de la factura", example = "100", required = true)
            @PathVariable Long id) {
        FacturaResponseDTO factura = facturaService.buscarPorId(id);
        return ResponseEntity.ok(factura);
    }

    @PatchMapping("/{id}/anular")
    @Operation(
            summary = "Anular factura emitida",
            description = "Cambia el estado de una factura a ANULADA dejando trazabilidad contable."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura anulada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FacturaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: la factura ya se encuentra anulada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<FacturaResponseDTO> anularFactura(
            @Parameter(description = "Identificador único de la factura a anular", example = "100", required = true)
            @PathVariable Long id) {
        FacturaResponseDTO facturaAnulada = facturaService.anularFactura(id);
        return ResponseEntity.ok(facturaAnulada);
    }

}
