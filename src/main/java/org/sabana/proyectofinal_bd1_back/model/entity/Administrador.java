package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad de dominio Administrador que hereda 1:1 de Staff.
 * POJO puro sin anotaciones de persistencia JPA.
 * El Administrador siempre se encuentra activo en el sistema.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Administrador extends Staff {

}
