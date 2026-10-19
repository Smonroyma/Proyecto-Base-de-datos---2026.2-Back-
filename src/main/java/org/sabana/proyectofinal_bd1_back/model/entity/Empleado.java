package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad hija Empleado (Mesero/Operativo) que hereda 1:1 de Staff.
 * Incluye el atributo estado (ACTIVO / INACTIVO).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Empleado extends Staff {

    private String estado; // ACTIVO, INACTIVO

    public boolean isActivo() {
        return estado == null || "ACTIVO".equalsIgnoreCase(estado.trim());
    }

}
