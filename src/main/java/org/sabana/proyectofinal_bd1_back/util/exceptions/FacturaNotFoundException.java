package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando una factura solicitada no existe en el sistema.
 */
public class FacturaNotFoundException extends ResourceNotFoundException {

    public FacturaNotFoundException(Long idFactura) {
        super(String.format("No se encontró la factura con ID: %d", idFactura));
    }

    public FacturaNotFoundException(String message) {
        super(message);
    }

}
