package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Enumeración con los estados del ciclo de vida de un Pedido en Savana Data Coffee.
 */
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
