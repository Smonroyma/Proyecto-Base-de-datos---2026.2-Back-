package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Roles operativos de Staff en el sistema Savana Data Coffee.
 * Determinados según la tabla hija (administrador o mesero).
 */
public enum RolUsuario {
    ADMINISTRADOR,
    MESERO;

    public static boolean esValido(String rol) {
        if (rol == null) {
            return false;
        }
        for (RolUsuario r : values()) {
            if (r.name().equalsIgnoreCase(rol.trim())) {
                return true;
            }
        }
        return false;
    }
}
