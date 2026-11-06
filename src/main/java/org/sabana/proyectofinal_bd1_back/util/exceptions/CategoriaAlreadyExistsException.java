package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class CategoriaAlreadyExistsException extends DuplicateResourceException {

    public CategoriaAlreadyExistsException(String message) {
        super(message);
    }

}
