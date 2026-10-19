package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sabana.proyectofinal_bd1_back.model.dto.AuthResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.LoginRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.UsuarioRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.Verificar2FARequestDTO;
import org.sabana.proyectofinal_bd1_back.service.AuthService;
import org.sabana.proyectofinal_bd1_back.util.exceptions.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación del Staff y verificación en dos pasos (2FA) con OTP en memoria.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación & 2FA", description = "Endpoints para inicio de sesión seguro del Staff con JWT y validación OTP en memoria")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Registro administrativo de nuevo miembro de Staff",
            description = "Registra un nuevo empleado o administrador en el sistema (Exclusivo para ADMINISTRADOR, el autorregistro público está cerrado)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Miembro de staff registrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol ADMINISTRADOR)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Correo electrónico ya registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<AuthResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO requestDTO) {
        AuthResponseDTO response = authService.registrar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Inicio de sesión primario del Staff",
            description = "Valida credenciales de Administrador o Empleado activo. Si tiene 2FA habilitado, genera un OTP en memoria (ConcurrentHashMap) y lo envía por correo HTML."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso o requerimiento de 2FA emitido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Formato de credenciales incorrecto",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o empleado inactivo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        AuthResponseDTO response = authService.login(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verificar-2fa")
    @Operation(
            summary = "Validación de código OTP (2FA en memoria)",
            description = "Valida el código de 6 dígitos almacenado temporalmente en memoria (5 minutos). Al ser válido, emite el token JWT con rol ADMINISTRADOR o EMPLEADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación 2FA exitosa y emisión de token JWT",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Código OTP incorrecto o expirado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autorizado o empleado inactivo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<AuthResponseDTO> verificar2FA(@Valid @RequestBody Verificar2FARequestDTO requestDTO) {
        AuthResponseDTO response = authService.verificar2FA(requestDTO);
        return ResponseEntity.ok(response);
    }

}
