package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class FacturaNotFoundException extends ResourceNotFoundException {

    public FacturaNotFoundException(Long idFactura) {
        super(String.format("No se encontró la factura con ID: %d", idFactura));
    }

    public FacturaNotFoundException(String message) {
        super(message);
    }

}
