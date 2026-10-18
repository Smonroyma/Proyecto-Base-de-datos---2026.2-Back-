package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta emitir una factura para un pedido que ya fue facturado.
 */
public class PedidoYaFacturadoException extends DuplicateResourceException {

    public PedidoYaFacturadoException(Long idPedido) {
        super(String.format("El pedido con ID %d ya cuenta con una factura activa emitida", idPedido));
    }

    public PedidoYaFacturadoException(String message) {
        super(message);
    }

}
