package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando el código OTP de verificación en dos pasos ha superado su tiempo de validez.
 */
public class OtpExpiredException extends RuntimeException {

    public OtpExpiredException(String message) {
        super(message);
    }

}
