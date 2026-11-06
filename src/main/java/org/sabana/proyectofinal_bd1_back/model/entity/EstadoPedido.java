package org.sabana.proyectofinal_bd1_back.model.entity;

public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    ENTREGADO,
    CANCELADO,
    PAGADO;

    public static boolean esValido(String estado) {
        if (estado == null) {
            return false;
        }
        for (EstadoPedido e : values()) {
            if (e.name().equalsIgnoreCase(estado.trim())) {
                return true;
            }
        }
        return false;
    }
}
