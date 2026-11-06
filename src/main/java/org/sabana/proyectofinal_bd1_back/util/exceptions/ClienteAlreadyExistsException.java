package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class ClienteAlreadyExistsException extends DuplicateResourceException {

    public ClienteAlreadyExistsException(String message) {
        super(message);
    }

}
