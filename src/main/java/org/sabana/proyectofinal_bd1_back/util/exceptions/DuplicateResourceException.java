package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción base para violaciones de unicidad o recursos duplicados.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

}
