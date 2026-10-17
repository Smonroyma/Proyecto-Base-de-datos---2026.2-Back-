package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar una categoría con un nombre que ya existe.
 */
public class CategoriaAlreadyExistsException extends DuplicateResourceException {

    public CategoriaAlreadyExistsException(String message) {
        super(message);
    }

}
