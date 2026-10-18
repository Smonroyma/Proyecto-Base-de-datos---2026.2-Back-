package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio MetodoPago.
 * Mapea directamente la tabla de base de datos 'metodo_pago' sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {

    private Long idMetodoPago;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
