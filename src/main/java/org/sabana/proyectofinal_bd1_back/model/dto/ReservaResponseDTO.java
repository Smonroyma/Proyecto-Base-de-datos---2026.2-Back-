package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de salida con los datos completos de una reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada con los datos de una reserva en Savana Data Coffee")
public class ReservaResponseDTO {

    @Schema(description = "Identificador único de la reserva", example = "1")
    private Long idReserva;

    @Schema(description = "ID del cliente asociado a la reserva", example = "1")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente", example = "Carlos Mendoza")
    private String nombreCliente;

    @Schema(description = "ID de la mesa asignada", example = "3")
    private Long idMesa;

    @Schema(description = "Número visible de la mesa asignada", example = "5")
    private Integer numeroMesa;

    @Schema(description = "Fecha y hora reservada", example = "2026-11-20T19:30:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Cantidad de personas reservadas", example = "4")
    private Integer numeroPersonas;

    @Schema(description = "Estado actual de la reserva", example = "CONFIRMADA")
    private String estado;

    @Schema(description = "Observaciones o peticiones especiales", example = "Celebración de aniversario, mesa junto a la ventana")
    private String observaciones;

}
