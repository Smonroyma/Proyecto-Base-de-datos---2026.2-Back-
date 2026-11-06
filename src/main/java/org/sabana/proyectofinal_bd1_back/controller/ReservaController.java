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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Endpoints para la gestión, control de disponibilidad, búsqueda por correo único y asignación de mesas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar reservas",
            description = "Obtiene la lista de reservas registradas en el sistema. Permite filtrar opcionalmente por correo único del cliente, ID de cliente, número de mesa y estado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class))))
    })
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservas(
            @Parameter(description = "Correo electrónico único del cliente para filtrar sus reservas", example = "carlos.mendoza@email.com")
            @RequestParam(name = "correo", required = false) String correo,
            @Parameter(description = "Identificador opcional del cliente para filtrar", example = "1")
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @Parameter(description = "Número opcional de la mesa para filtrar", example = "3")
            @RequestParam(name = "numeroMesa", required = false) Integer numeroMesa,
            @Parameter(description = "Alias opcional de mesa para filtrar", example = "3")
            @RequestParam(name = "mesaId", required = false) Integer mesaIdAlias,
            @Parameter(description = "Estado opcional de la reserva para filtrar (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)", example = "CONFIRMADA")
            @RequestParam(name = "estado", required = false) String estado) {
        Integer mesaFiltro = numeroMesa != null ? numeroMesa : mesaIdAlias;
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservas(clienteId, correo, mesaFiltro, estado);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/correo/{correo}")
    @Operation(
            summary = "Listar reservas por correo único del cliente",
            description = "Permite al cliente (público) o al Staff consultar todas las reservas asociadas a un correo electrónico único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas del cliente obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class))))
    })
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorCorreo(
            @Parameter(description = "Correo electrónico del cliente", example = "carlos.mendoza@email.com", required = true)
            @PathVariable String correo) {
        return ResponseEntity.ok(reservaService.buscarPorCorreoCliente(correo));
    }

    @GetMapping("/disponibles")
    @Operation(
            summary = "Consultar mesas disponibles",
            description = "Retorna las mesas libres que cumplen con la capacidad requerida y no presentan solapamiento de horarios en la fecha y hora especificadas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesas disponibles obtenidas exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MesaResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos o fecha en el pasado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<MesaResponseDTO>> consultarMesasDisponibles(
            @Parameter(description = "Fecha y hora solicitada para la reserva (formato ISO-8601)", example = "2026-11-20T19:30:00")
            @RequestParam(name = "fechaHora", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
            @Parameter(description = "Fecha solicitada (alternativa si se envía separada de hora)", example = "2026-11-20")
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Hora solicitada (alternativa si se envía separada de fecha)", example = "19:30:00")
            @RequestParam(name = "hora", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @Parameter(description = "Cantidad de personas requerida", example = "4", required = true)
            @RequestParam(name = "numeroPersonas") Integer numeroPersonas) {
        LocalDateTime fechaHoraEfectiva = fechaHora;
        if (fechaHoraEfectiva == null && fecha != null && hora != null) {
            fechaHoraEfectiva = LocalDateTime.of(fecha, hora);
        }
        List<MesaResponseDTO> mesasDisponibles = reservaService.consultarMesasDisponibles(fechaHoraEfectiva, numeroPersonas);
        return ResponseEntity.ok(mesasDisponibles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por ID", description = "Retorna la información detallada de una reserva según su identificador único.")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único de la reserva", example = "1", required = true)
            @PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.buscarPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @PostMapping
    @Operation(
            summary = "Crear nueva reserva (Público / Salón)",
            description = "Registra una nueva reserva. Si el cliente envía sus datos y su correo no existe en CLIENTE, se crea y guarda permanentemente; si ya existe, se reutiliza su id_cliente."
    )
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
            description = "Actualiza el estado de la reserva (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)."
    )
    public ResponseEntity<ReservaResponseDTO> cambiarEstado(
            @Parameter(description = "Identificador único de la reserva", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado para la reserva (query param)", example = "CONFIRMADA")
            @RequestParam(name = "nuevoEstado", required = false) String nuevoEstadoParam,
            @RequestParam(name = "estado", required = false) String estadoParam,
            @RequestBody(required = false) Map<String, String> body) {
        String estadoEfectivo = nuevoEstadoParam != null && !nuevoEstadoParam.isBlank() ? nuevoEstadoParam : estadoParam;
        if ((estadoEfectivo == null || estadoEfectivo.isBlank()) && body != null) {
            estadoEfectivo = body.getOrDefault("nuevoEstado", body.get("estado"));
        }
        if (estadoEfectivo == null || estadoEfectivo.isBlank()) {
            throw new IllegalArgumentException("El nuevo estado de la reserva es obligatorio");
        }
        ReservaResponseDTO reservaActualizada = reservaService.cambiarEstado(id, estadoEfectivo);
        return ResponseEntity.ok(reservaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reserva", description = "Elimina permanentemente una reserva del sistema según su ID (conservando siempre el registro del cliente).")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único de la reserva a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
