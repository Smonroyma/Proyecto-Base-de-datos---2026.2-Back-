package org.sabana.proyectofinal_bd1_back.model.entity;

public enum EstadoFactura {
    EMITIDA,
    ANULADA,
    PAGADA;

    public static boolean esValido(String estado) {
        if (estado == null) {
            return false;
        }
        for (EstadoFactura e : values()) {
            if (e.name().equalsIgnoreCase(estado.trim())) {
                return true;
            }
        }
        return false;
    }
}
