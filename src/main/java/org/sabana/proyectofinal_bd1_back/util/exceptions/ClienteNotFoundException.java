package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando un cliente solicitado no existe en el sistema.
 */
public class ClienteNotFoundException extends ResourceNotFoundException {

    public ClienteNotFoundException(Long idCliente) {
        super(String.format("No se encontró el cliente con ID: %d", idCliente));
    }

    public ClienteNotFoundException(String message) {
        super(message);
    }

}
