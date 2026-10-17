package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando ocurre un conflicto de disponibilidad, solapamiento o capacidad en una reserva.
 */
public class ReservaConflictException extends DuplicateResourceException {

    public ReservaConflictException(String message) {
        super(message);
    }

}
