package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio Mesa.
 * Mapea directamente la tabla mesa de la base de datos sin anotaciones JPA.
 * La llave primaria es numero_mesa (Integer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    private Integer numeroMesa;
    private Integer capacidad;
    private String estado;

}
