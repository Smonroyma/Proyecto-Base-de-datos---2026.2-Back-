package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad de dominio Reserva.
 * Mapea directamente la tabla de base de datos 'reserva' sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    private Long idReserva;
    private Long idCliente;
    private String nombreCliente;
    private String correoCliente;
    private Integer numeroMesa;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer numeroPersonas;
    private String estado;
    private String observaciones;

    public LocalDateTime getFechaHora() {
        if (fecha != null && hora != null) {
            return LocalDateTime.of(fecha, hora);
        }
        return null;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        if (fechaHora != null) {
            this.fecha = fechaHora.toLocalDate();
            this.hora = fechaHora.toLocalTime();
        }
    }

}
