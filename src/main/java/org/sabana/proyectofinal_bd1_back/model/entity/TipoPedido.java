package org.sabana.proyectofinal_bd1_back.model.entity;

/**
 * Enumeración con los tipos de pedido soportados en Savana Data Coffee.
 */
public enum TipoPedido {
    MESA,
    LLEVAR;

    public static boolean esValido(String tipo) {
        if (tipo == null) {
            return false;
        }
        for (TipoPedido t : values()) {
            if (t.name().equalsIgnoreCase(tipo.trim())) {
                return true;
            }
        }
        return false;
    }
}
