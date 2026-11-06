package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class MesaAlreadyExistsException extends DuplicateResourceException {

    public MesaAlreadyExistsException(String message) {
        super(message);
    }

}
