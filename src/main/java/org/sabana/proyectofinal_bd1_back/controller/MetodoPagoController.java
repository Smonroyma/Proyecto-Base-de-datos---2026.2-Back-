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
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.MetodoPagoService;
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
 * Controlador REST para la administración de métodos de pago en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/metodos-pago")
@Tag(name = "Métodos de Pago", description = "Endpoints para la configuración y consulta de medios de pago habilitados en caja")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar métodos de pago",
            description = "Obtiene el catálogo de métodos de pago registrados. Permite filtrar opcionalmente solo aquellos activos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de métodos de pago obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MetodoPagoResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<MetodoPagoResponseDTO>> obtenerMetodosPago(
            @Parameter(description = "Si es true, retorna únicamente los métodos de pago activos", example = "true")
            @RequestParam(name = "soloActivos", required = false) Boolean soloActivos) {
        List<MetodoPagoResponseDTO> metodos = metodoPagoService.obtenerMetodosPago(soloActivos);
        return ResponseEntity.ok(metodos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar método de pago por ID",
            description = "Retorna el detalle de un método de pago específico según su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Método de pago encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetodoPagoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Método de pago no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MetodoPagoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único del método de pago", example = "1", required = true)
            @PathVariable Long id) {
        MetodoPagoResponseDTO metodo = metodoPagoService.buscarPorId(id);
        return ResponseEntity.ok(metodo);
    }

    @PostMapping
    @Operation(
            summary = "Registrar nuevo método de pago",
            description = "Crea un nuevo método de pago en el sistema validando la unicidad de su código."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Método de pago creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetodoPagoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe un método de pago con ese código",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MetodoPagoResponseDTO> crear(
            @Valid @RequestBody MetodoPagoRequestDTO requestDTO) {
        MetodoPagoResponseDTO creado = metodoPagoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar método de pago",
            description = "Actualiza los datos y estado de un método de pago existente validando que su código no se duplique."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Método de pago actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetodoPagoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Método de pago no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe otro método de pago con ese código",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<MetodoPagoResponseDTO> actualizar(
            @Parameter(description = "Identificador único del método de pago a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MetodoPagoRequestDTO requestDTO) {
        MetodoPagoResponseDTO actualizado = metodoPagoService.actualizar(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar método de pago",
            description = "Elimina permanentemente un método de pago del catálogo según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Método de pago eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Método de pago no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único del método de pago a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        metodoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
