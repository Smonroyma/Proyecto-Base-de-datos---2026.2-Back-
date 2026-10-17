package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando una mesa solicitada no existe en el sistema.
 */
public class MesaNotFoundException extends ResourceNotFoundException {

    public MesaNotFoundException(Long idMesa) {
        super(String.format("No se encontró la mesa con ID: %d", idMesa));
    }

    public MesaNotFoundException(String message) {
        super(message);
    }

}
