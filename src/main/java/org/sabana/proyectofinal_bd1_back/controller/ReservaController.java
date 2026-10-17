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
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ReservaResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.ReservaService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de reservas en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Endpoints para la gestión, control de disponibilidad y asignación de mesas para reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar reservas",
            description = "Obtiene la lista de reservas registradas en el sistema. Permite filtrar opcionalmente por cliente, mesa y estado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservas(
            @Parameter(description = "Identificador opcional del cliente para filtrar", example = "1")
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @Parameter(description = "Identificador opcional de la mesa para filtrar", example = "3")
            @RequestParam(name = "mesaId", required = false) Long mesaId,
            @Parameter(description = "Estado opcional de la reserva para filtrar (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)", example = "CONFIRMADA")
            @RequestParam(name = "estado", required = false) String estado) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservas(clienteId, mesaId, estado);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/disponibles")
    @Operation(
            summary = "Consultar mesas disponibles",
            description = "Retorna las mesas libres que cumplen con la capacidad requerida y no presentan solapamiento de horarios en la fecha especificada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesas disponibles obtenidas exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MesaResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos o fecha en el pasado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<MesaResponseDTO>> consultarMesasDisponibles(
            @Parameter(description = "Fecha y hora solicitada para la reserva (formato ISO-8601)", example = "2026-11-20T19:30:00", required = true)
            @RequestParam(name = "fechaHora") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
            @Parameter(description = "Cantidad de personas requerida", example = "4", required = true)
            @RequestParam(name = "numeroPersonas") Integer numeroPersonas) {
        List<MesaResponseDTO> mesasDisponibles = reservaService.consultarMesasDisponibles(fechaHora, numeroPersonas);
        return ResponseEntity.ok(mesasDisponibles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por ID", description = "Retorna la información detallada de una reserva según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único de la reserva", example = "1", required = true)
            @PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.buscarPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @PostMapping
    @Operation(
            summary = "Crear nueva reserva",
            description = "Registra una nueva reserva validando existencia del cliente, de la mesa, capacidad suficiente y ausencia de solapamiento de horario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente o mesa no encontrados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por solapamiento de horario o capacidad insuficiente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> crear(
            @Valid @RequestBody ReservaRequestDTO requestDTO) {
        ReservaResponseDTO reservaCreada = reservaService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaCreada);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar reserva existente",
            description = "Actualiza los datos de una reserva validando consistencia de cliente, mesa, horario y capacidad."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva, cliente o mesa no encontrados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por solapamiento de horario o capacidad",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> actualizar(
            @Parameter(description = "Identificador único de la reserva a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ReservaRequestDTO requestDTO) {
        ReservaResponseDTO reservaActualizada = reservaService.actualizar(id, requestDTO);
        return ResponseEntity.ok(reservaActualizada);
    }

    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar estado de una reserva",
            description = "Actualiza el estado de la reserva (ej. CONFIRMADA, CANCELADA, COMPLETADA)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de reserva actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado especificado no es válido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ReservaResponseDTO> cambiarEstado(
            @Parameter(description = "Identificador único de la reserva", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado para la reserva", example = "CONFIRMADA", required = true)
            @RequestParam(name = "nuevoEstado") String nuevoEstado) {
        ReservaResponseDTO reservaActualizada = reservaService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(reservaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reserva", description = "Elimina permanentemente una reserva del sistema según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único de la reserva a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
