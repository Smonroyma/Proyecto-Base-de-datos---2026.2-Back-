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
 * POJO puro sin anotaciones de persistencia JPA.
 * Representa la tabla 'reserva' con clave compuesta unida de forma simple (idReserva_idCliente) sin clases adicionales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    private String idReservaCompuesta;
    private Long idReserva;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer numeroPersonas;
    private String estado;

    private Long idCliente;
    private String nombreCliente;
    private String correoCliente;
    private Integer numeroMesa;

    public void unirClaveCompuesta(Long idReserva, Long idCliente) {
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        if (idReserva != null && idCliente != null) {
            this.idReservaCompuesta = idReserva + "_" + idCliente;
        }
    }

    public String getIdReservaCompuesta() {
        if (idReservaCompuesta == null && idReserva != null && idCliente != null) {
            return idReserva + "_" + idCliente;
        }
        return idReservaCompuesta;
    }

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
