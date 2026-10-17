package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Roles operativos de usuarios en el sistema Savana Data Coffee.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    BARISTA,
    CAJERO;

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
