package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.UsuarioService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión y administración de usuarios.
 * Protegido para acceso exclusivo de usuarios con rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints administrativos para la gestión de usuarios (Requiere rol ADMINISTRADOR)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene la lista completa de empleados y administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMINISTRADOR)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Retorna los datos de un usuario dado su ID único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único del usuario", example = "1", required = true)
            @PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente", description = "Modifica los datos de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Correo ya en uso por otro usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @Parameter(description = "Identificador único del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO requestDTO) {
        UsuarioResponseDTO actualizado = usuarioService.actualizar(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina permanentemente un usuario del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
