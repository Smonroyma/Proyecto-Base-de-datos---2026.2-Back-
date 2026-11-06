package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada con los datos de una reserva en Savana Data Coffee")
public class ReservaResponseDTO {

    @Schema(description = "Clave primaria compuesta unida en el backend (idReserva_idCliente)", example = "1_1")
    private String idReservaCompuesta;

    @Schema(description = "Identificador único de la reserva", example = "1")
    private Long idReserva;

    @Schema(description = "ID del cliente asociado a la reserva", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente", example = "Carlos Mendoza")
    private String nombreCliente;

    @Schema(description = "Correo electrónico único del cliente", example = "carlos.mendoza@email.com")
    private String correoCliente;

    @Schema(description = "Número único de la mesa asignada", example = "3")
    private Integer numeroMesa;

    @Schema(description = "Fecha reservada", example = "2026-11-20")
    private LocalDate fecha;

    @Schema(description = "Hora reservada", example = "19:30:00")
    private LocalTime hora;

    @Schema(description = "Fecha y hora reservada combinada", example = "2026-11-20T19:30:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Cantidad de personas reservadas", example = "4")
    private Integer numeroPersonas;

    @Schema(description = "Estado actual de la reserva", example = "CONFIRMADA")
    private String estado;

}
