package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio Mesa.
 * Mapea directamente la tabla de base de datos sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    private Long idMesa;
    private Integer numero;
    private Integer capacidad;
    private String estado;

}
