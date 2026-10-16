package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar un cliente con datos ya existentes (ej. email o teléfono).
 */
public class ClienteAlreadyExistsException extends DuplicateResourceException {

    public ClienteAlreadyExistsException(String message) {
        super(message);
    }

}
