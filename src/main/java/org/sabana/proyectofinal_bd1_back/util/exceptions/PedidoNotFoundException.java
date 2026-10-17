package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando un pedido solicitado no existe en el sistema.
 */
public class PedidoNotFoundException extends ResourceNotFoundException {

    public PedidoNotFoundException(Long idPedido) {
        super(String.format("No se encontró el pedido con ID: %d", idPedido));
    }

    public PedidoNotFoundException(String message) {
        super(message);
    }

}
