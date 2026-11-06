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
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.ProductoResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.ProductoService;
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

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para la administración del catálogo de productos de la cafetería")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Obtiene la lista de productos del catálogo. Permite filtrar opcionalmente por nombre de categoría y por disponibilidad."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductoResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductos(
            @Parameter(description = "Nombre opcional de la categoría para filtrar", example = "Bebidas Calientes")
            @RequestParam(name = "nombreCategoria", required = false) String nombreCategoria,
            @Parameter(description = "Alias opcional de categoría para filtrar", example = "Bebidas Calientes")
            @RequestParam(name = "categoria", required = false) String categoriaAlias,
            @Parameter(description = "Estado de disponibilidad opcional para filtrar", example = "true")
            @RequestParam(name = "disponibilidad", required = false) Boolean disponibilidad,
            @Parameter(description = "Alias opcional de disponibilidad para filtrar", example = "true")
            @RequestParam(name = "disponible", required = false) Boolean disponibleAlias) {
        String cat = (nombreCategoria != null && !nombreCategoria.isBlank()) ? nombreCategoria : categoriaAlias;
        Boolean disp = disponibilidad != null ? disponibilidad : disponibleAlias;
        List<ProductoResponseDTO> productos = productoService.obtenerProductos(cat, disp);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por ID", description = "Retorna la información detallada de un producto según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ProductoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único del producto", example = "1", required = true)
            @PathVariable Long id) {
        ProductoResponseDTO producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo producto",
            description = "Registra un nuevo producto en el catálogo validando la existencia de la categoría y la unicidad del nombre."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría referenciada no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por nombre de producto ya existente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ProductoResponseDTO> crear(
            @Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO productoCreado = productoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar producto existente",
            description = "Actualiza los atributos de un producto existente validando que la categoría exista y que el nombre no entre en conflicto con otro producto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe otro producto registrado con ese nombre",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @Parameter(description = "Identificador único del producto a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO productoActualizado = productoService.actualizar(id, requestDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina permanentemente un producto del catálogo según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado con el ID especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único del producto a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
