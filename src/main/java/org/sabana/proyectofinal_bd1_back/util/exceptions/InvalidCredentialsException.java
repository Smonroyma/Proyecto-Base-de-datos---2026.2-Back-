package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando las credenciales (correo o contraseña) son inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

}
