package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Long idProducto;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean disponibilidad;
    private Integer stock;
    private String imagenUrl;
    private String nombreCategoria;

}
