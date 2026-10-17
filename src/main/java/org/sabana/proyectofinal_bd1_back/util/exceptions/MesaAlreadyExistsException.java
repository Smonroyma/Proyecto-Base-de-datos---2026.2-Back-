package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar una mesa con un número ya existente.
 */
public class MesaAlreadyExistsException extends DuplicateResourceException {

    public MesaAlreadyExistsException(String message) {
        super(message);
    }

}
