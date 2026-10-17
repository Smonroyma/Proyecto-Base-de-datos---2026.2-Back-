package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de entrada para la solicitud de creación y actualización de reservas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de una reserva en Savana Data Coffee")
public class ReservaRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Schema(description = "Identificador único del cliente que realiza la reserva", example = "1")
    private Long idCliente;

    @NotNull(message = "El ID de la mesa es obligatorio")
    @Schema(description = "Identificador único de la mesa asignada para la reserva", example = "3")
    private Long idMesa;

    @NotNull(message = "La fecha y hora de la reserva es obligatoria")
    @Future(message = "La fecha y hora de la reserva debe ser una fecha y hora futura")
    @Schema(description = "Fecha y hora programada para la reserva", example = "2026-11-20T19:30:00")
    private LocalDateTime fechaHora;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "El número de personas debe ser al menos 1")
    @Schema(description = "Cantidad de personas que asistirán a la reserva", example = "4")
    private Integer numeroPersonas;

    @Pattern(regexp = "^(?i)(PENDIENTE|CONFIRMADA|CANCELADA|COMPLETADA)$", message = "El estado debe ser PENDIENTE, CONFIRMADA, CANCELADA o COMPLETADA")
    @Schema(description = "Estado inicial u operativo de la reserva", example = "PENDIENTE", allowableValues = {"PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA"})
    private String estado;

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
    @Schema(description = "Notas u observaciones especiales sobre la reserva", example = "Celebración de aniversario, mesa junto a la ventana")
    private String observaciones;

}
