package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class MetodoPagoNotFoundException extends ResourceNotFoundException {

    public MetodoPagoNotFoundException(Long idMetodoPago) {
        super(String.format("No se encontró el método de pago con ID: %d", idMetodoPago));
    }

    public MetodoPagoNotFoundException(String message) {
        super(message);
    }

}
