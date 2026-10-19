package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de entrada para la solicitud de creación y actualización de reservas.
 * Soporta tanto idCliente existente como registro/reutilización automática de cliente por correo único.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de una reserva en Savana Data Coffee")
public class ReservaRequestDTO {

    @Schema(description = "Identificador único del cliente (opcional si se envían los datos del cliente con correo)", example = "1")
    private Long idCliente;

    @JsonAlias({"nombre", "nombreCliente"})
    @Schema(description = "Nombres del cliente (para reserva pública sin cuenta)", example = "Carlos")
    private String nombres;

    @JsonAlias({"apellido", "apellidosCliente"})
    @Schema(description = "Apellidos del cliente (para reserva pública sin cuenta)", example = "Mendoza")
    private String apellidos;

    @Email(message = "El formato del correo electrónico del cliente no es válido")
    @JsonAlias({"email", "correoCliente"})
    @Schema(description = "Correo electrónico único del cliente", example = "carlos.mendoza@email.com")
    private String correo;

    @JsonAlias({"telefonoCliente"})
    @Schema(description = "Teléfono de contacto del cliente", example = "+57 300 1234567")
    private String telefono;

    @NotNull(message = "El número de la mesa es obligatorio")
    @JsonAlias({"idMesa", "mesa"})
    @Schema(description = "Número único de la mesa asignada para la reserva (PK de mesa)", example = "3")
    private Integer numeroMesa;

    @Schema(description = "Fecha programada para la reserva", example = "2026-11-20")
    private LocalDate fecha;

    @Schema(description = "Hora programada para la reserva", example = "19:30:00")
    private LocalTime hora;

    @Schema(description = "Fecha y hora programada para la reserva (compatible con ISO-8601)", example = "2026-11-20T19:30:00")
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

    public LocalDate resolveFecha() {
        if (fecha != null) {
            return fecha;
        }
        return fechaHora != null ? fechaHora.toLocalDate() : null;
    }

    public LocalTime resolveHora() {
        if (hora != null) {
            return hora;
        }
        return fechaHora != null ? fechaHora.toLocalTime() : null;
    }

    public LocalDateTime resolveFechaHora() {
        if (fechaHora != null) {
            return fechaHora;
        }
        if (fecha != null && hora != null) {
            return LocalDateTime.of(fecha, hora);
        }
        return null;
    }

}
