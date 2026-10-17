package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Enumeración con los estados permitidos para una Mesa en el sistema.
 */
public enum EstadoMesa {
    DISPONIBLE,
    OCUPADA,
    RESERVADA,
    EN_MANTENIMIENTO;

    public static boolean esValido(String estado) {
        if (estado == null) {
            return false;
        }
        for (EstadoMesa e : values()) {
            if (e.name().equalsIgnoreCase(estado.trim())) {
                return true;
            }
        }
        return false;
    }
}
