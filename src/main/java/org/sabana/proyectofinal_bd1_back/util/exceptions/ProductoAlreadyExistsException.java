package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class ProductoAlreadyExistsException extends DuplicateResourceException {

    public ProductoAlreadyExistsException(String message) {
        super(message);
    }

}
