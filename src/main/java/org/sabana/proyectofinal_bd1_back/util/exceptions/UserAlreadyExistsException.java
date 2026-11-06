package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class UserAlreadyExistsException extends DuplicateResourceException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
