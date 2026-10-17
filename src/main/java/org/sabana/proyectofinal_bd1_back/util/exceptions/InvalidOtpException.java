package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando el código OTP de verificación en dos pasos es incorrecto o inválido.
 */
public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String message) {
        super(message);
    }

}
