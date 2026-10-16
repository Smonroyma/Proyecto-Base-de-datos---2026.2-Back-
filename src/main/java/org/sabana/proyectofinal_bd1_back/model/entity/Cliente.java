package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Cliente.
 * Mapea directamente la tabla de base de datos sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Long idCliente;
    private String nombre;
    private String telefono;
    private String email;
    private LocalDateTime fechaRegistro;

}
