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
import org.sabana.proyectofinal_bd1_back.model.dto.ActualizarEstadoPedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PedidoResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.PedidoService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de pedidos y comandas en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para la creación, consulta, control de estados y cancelación de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo pedido",
            description = "Registra un nuevo pedido y sus detalles, congela los precios unitarios de cada producto, calcula el total y descuenta el inventario de forma transaccional."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido registrado y procesado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente, mesa, usuario o producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente o producto no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<PedidoResponseDTO> crearPedido(
            @Valid @RequestBody PedidoRequestDTO requestDTO) {
        PedidoResponseDTO pedidoCreado = pedidoService.crearPedido(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCreado);
    }

    @GetMapping
    @Operation(
            summary = "Consultar pedidos",
            description = "Obtiene la lista de pedidos con sus respectivos detalles. Permite filtrar opcionalmente por cliente y por estado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PedidoResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidos(
            @Parameter(description = "Identificador opcional del cliente para filtrar", example = "1")
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @Parameter(
                    description = "Estado opcional del pedido para filtrar",
                    example = "PENDIENTE",
                    schema = @Schema(allowableValues = {"PENDIENTE", "EN_PREPARACION", "ENTREGADO", "CANCELADO", "PAGADO"})
            )
            @RequestParam(name = "estado", required = false) String estado) {
        List<PedidoResponseDTO> pedidos = pedidoService.obtenerPedidos(clienteId, estado);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar pedido por ID",
            description = "Retorna la información completa de un pedido específico junto con la lista detallada de sus ítems ordenados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Actualizar estado del pedido",
            description = "Modifica el estado operativo del pedido (ej. de PENDIENTE a EN_PREPARACION, ENTREGADO o CANCELADO)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado no válido o transición de estado ilegal",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @Parameter(description = "Identificador único del pedido a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(required = false) ActualizarEstadoPedidoRequestDTO body,
            @Parameter(description = "Nuevo estado opcional por query parameter si no se envía body", example = "EN_PREPARACION")
            @RequestParam(name = "nuevoEstado", required = false) String paramEstado) {

        String estadoFinal = (body != null && body.getNuevoEstado() != null && !body.getNuevoEstado().isBlank())
                ? body.getNuevoEstado()
                : paramEstado;

        if (estadoFinal == null || estadoFinal.isBlank()) {
            throw new IllegalArgumentException("Debe especificar el nuevo estado del pedido mediante el cuerpo o parámetro 'nuevoEstado'");
        }

        PedidoResponseDTO pedidoActualizado = pedidoService.actualizarEstado(id, estadoFinal);
        return ResponseEntity.ok(pedidoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar pedido",
            description = "Elimina permanentemente un pedido y sus detalles asociados del sistema según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminarPedido(
            @Parameter(description = "Identificador único del pedido a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }

}
