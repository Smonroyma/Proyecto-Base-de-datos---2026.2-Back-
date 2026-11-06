package org.sabana.proyectofinal_bd1_back.model.entity;

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
