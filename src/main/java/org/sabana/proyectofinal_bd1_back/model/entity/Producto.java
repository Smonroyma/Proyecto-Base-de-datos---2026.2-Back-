package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio Producto.
 * Mapea directamente la tabla de base de datos 'producto' sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Long idProducto;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Long idCategoria;
    private String nombreCategoria;
    private String imagenUrl;
    private Boolean disponible;
    private Integer stock;

}
