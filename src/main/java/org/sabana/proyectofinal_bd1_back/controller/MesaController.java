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
import org.sabana.proyectofinal_bd1_back.model.dto.MesaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MesaResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.MesaService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de mesas en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/mesas")
@Tag(name = "Mesas", description = "Endpoints para la gestión, control de capacidad y estado de mesas en el salón")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar mesas",
            description = "Obtiene la lista completa de mesas o filtrada por estado operativo (DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mesas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MesaResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Parámetro de estado inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<MesaResponseDTO>> obtenerMesas(
            @Parameter(
                    description = "Estado operativo de la mesa para filtrar (opcional)",
                    example = "DISPONIBLE",
                    schema = @Schema(allowableValues = {"DISPONIBLE", "OCUPADA", "RESERVADA", "EN_MANTENIMIENTO"})
            )
            @RequestParam(name = "estado", required = false) String estado) {
        List<MesaResponseDTO> mesas = mesaService.obtenerMesas(estado);
        return ResponseEntity.ok(mesas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mesa por ID", description = "Retorna la información detallada de una mesa según su ID único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa encontrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MesaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MesaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único de la mesa", example = "1", required = true)
            @PathVariable Long id) {
        MesaResponseDTO mesa = mesaService.buscarPorId(id);
        return ResponseEntity.ok(mesa);
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Buscar mesa por número de salón", description = "Retorna la información de una mesa dado su número visible en el salón.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa encontrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MesaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada con el número especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MesaResponseDTO> buscarPorNumero(
            @Parameter(description = "Número visible de la mesa en el salón", example = "5", required = true)
            @PathVariable Integer numero) {
        MesaResponseDTO mesa = mesaService.buscarPorNumero(numero);
        return ResponseEntity.ok(mesa);
    }

    @PostMapping
    @Operation(summary = "Crear nueva mesa", description = "Registra una nueva mesa verificando que el número no se encuentre repetido y que el estado sea válido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mesa creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MesaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o estado no reconocido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por número de mesa duplicado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MesaResponseDTO> crear(
            @Valid @RequestBody MesaRequestDTO requestDTO) {
        MesaResponseDTO mesaCreada = mesaService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar mesa existente", description = "Actualiza el número, capacidad o estado de una mesa validando la unicidad del número.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MesaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o estado no reconocido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe otra mesa con ese número",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MesaResponseDTO> actualizar(
            @Parameter(description = "Identificador único de la mesa a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MesaRequestDTO requestDTO) {
        MesaResponseDTO mesaActualizada = mesaService.actualizar(id, requestDTO);
        return ResponseEntity.ok(mesaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mesa", description = "Elimina permanentemente una mesa del sistema dada su clave primaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mesa eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único de la mesa a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
