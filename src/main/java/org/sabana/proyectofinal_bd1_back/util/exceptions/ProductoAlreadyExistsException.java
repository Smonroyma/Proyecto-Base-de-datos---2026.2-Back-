package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar un producto con un nombre que ya existe.
 */
public class ProductoAlreadyExistsException extends DuplicateResourceException {

    public ProductoAlreadyExistsException(String message) {
        super(message);
    }

}
