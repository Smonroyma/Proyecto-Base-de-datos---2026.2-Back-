package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class ReservaNotFoundException extends ResourceNotFoundException {

    public ReservaNotFoundException(Long idReserva) {
        super(String.format("No se encontró la reserva con ID: %d", idReserva));
    }

    public ReservaNotFoundException(String message) {
        super(message);
    }

}
