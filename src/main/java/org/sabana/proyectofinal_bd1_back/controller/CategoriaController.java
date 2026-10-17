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
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.CategoriaResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.CategoriaService;
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
 * Controlador REST para la gestión de categorías en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Endpoints para la administración del catálogo de categorías de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Obtiene la lista de categorías del catálogo. Permite filtrar opcionalmente solo aquellas activas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoriaResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerCategorias(
            @Parameter(description = "Si es true, retorna únicamente las categorías con estado activo", example = "true")
            @RequestParam(name = "soloActivas", required = false) Boolean soloActivas) {
        List<CategoriaResponseDTO> categorias = categoriaService.obtenerCategorias(soloActivas);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoría por ID", description = "Retorna la información detallada de una categoría según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único de la categoría", example = "1", required = true)
            @PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping
    @Operation(summary = "Crear nueva categoría", description = "Registra una nueva categoría en el catálogo validando la unicidad del nombre.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por nombre de categoría ya existente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> crear(
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        CategoriaResponseDTO categoriaCreada = categoriaService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría existente", description = "Actualiza los datos de una categoría existente validando que el nombre no se duplique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe otra categoría con ese nombre",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @Parameter(description = "Identificador único de la categoría a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        CategoriaResponseDTO categoriaActualizada = categoriaService.actualizar(id, requestDTO);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina permanentemente una categoría del catálogo según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único de la categoría a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
