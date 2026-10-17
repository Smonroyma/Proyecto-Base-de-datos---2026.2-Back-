package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email ya existente.
 */
public class UserAlreadyExistsException extends DuplicateResourceException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
