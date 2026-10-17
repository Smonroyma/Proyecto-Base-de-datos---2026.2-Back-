package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando una reserva solicitada no existe en el sistema.
 */
public class ReservaNotFoundException extends ResourceNotFoundException {

    public ReservaNotFoundException(Long idReserva) {
        super(String.format("No se encontró la reserva con ID: %d", idReserva));
    }

    public ReservaNotFoundException(String message) {
        super(message);
    }

}
