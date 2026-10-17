package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Long idMesa;
    private Integer numeroMesa;
    private LocalDateTime fechaHora;
    private Integer numeroPersonas;
    private String estado;
    private String observaciones;

}
