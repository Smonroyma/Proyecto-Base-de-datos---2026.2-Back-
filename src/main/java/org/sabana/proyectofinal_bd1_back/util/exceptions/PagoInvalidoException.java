package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando ocurre un error de validación en el cuadre de pagos o método de pago inactivo.
 */
public class PagoInvalidoException extends RuntimeException {

    public PagoInvalidoException(String message) {
        super(message);
    }

}
