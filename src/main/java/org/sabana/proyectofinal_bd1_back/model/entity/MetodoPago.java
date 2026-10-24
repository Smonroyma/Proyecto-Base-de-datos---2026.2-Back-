package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio MetodoPago.
 * POJO puro sin anotaciones de persistencia JPA.
 * Tiene un único atributo: tipo (EFECTIVO, TARJETA, TRANSFERENCIA).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {

    private String tipo;

}
