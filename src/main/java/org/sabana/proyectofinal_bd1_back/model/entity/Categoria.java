package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio Categoria.
 * Mapea directamente la tabla de base de datos 'categoria' sin anotaciones JPA.
 * La llave primaria es nombre_categoria (String).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    private String nombreCategoria;
    private String estado;
    private String descripcion;

}
