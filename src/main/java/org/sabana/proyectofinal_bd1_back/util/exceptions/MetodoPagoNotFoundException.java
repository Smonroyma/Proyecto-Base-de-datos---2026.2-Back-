package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando un método de pago solicitado no existe en el sistema.
 */
public class MetodoPagoNotFoundException extends ResourceNotFoundException {

    public MetodoPagoNotFoundException(Long idMetodoPago) {
        super(String.format("No se encontró el método de pago con ID: %d", idMetodoPago));
    }

    public MetodoPagoNotFoundException(String message) {
        super(message);
    }

}
