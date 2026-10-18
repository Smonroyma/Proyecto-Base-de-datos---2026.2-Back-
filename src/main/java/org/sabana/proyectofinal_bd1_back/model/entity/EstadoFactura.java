package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Enumeración con los estados del ciclo de vida de una Factura en Savana Data Coffee.
 */
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
