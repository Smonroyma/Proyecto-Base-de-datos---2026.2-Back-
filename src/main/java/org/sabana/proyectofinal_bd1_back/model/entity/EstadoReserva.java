package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Enumeración con los estados permitidos para una Reserva en Savana Data Coffee.
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    COMPLETADA;

    public static boolean esValido(String estado) {
        if (estado == null) {
            return false;
        }
        for (EstadoReserva e : values()) {
            if (e.name().equalsIgnoreCase(estado.trim())) {
                return true;
            }
        }
        return false;
    }
}
