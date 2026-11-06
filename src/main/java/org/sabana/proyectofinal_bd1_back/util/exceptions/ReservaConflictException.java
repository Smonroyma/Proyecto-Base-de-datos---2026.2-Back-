package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class ReservaConflictException extends DuplicateResourceException {

    public ReservaConflictException(String message) {
        super(message);
    }

}
