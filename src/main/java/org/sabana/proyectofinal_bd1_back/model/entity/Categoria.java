package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio Categoria.
 * POJO puro sin anotaciones de persistencia JPA.
 * La llave primaria es nombre_categoria (String).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    private String nombreCategoria;
    private String descripcion;
    private String estado;

}
