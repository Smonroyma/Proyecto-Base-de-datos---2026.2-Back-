package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción base para recursos no encontrados en la base de datos.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
