package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando una categoría solicitada no existe en el sistema.
 */
public class CategoriaNotFoundException extends ResourceNotFoundException {

    public CategoriaNotFoundException(Long idCategoria) {
        super(String.format("No se encontró la categoría con ID: %d", idCategoria));
    }

    public CategoriaNotFoundException(String message) {
        super(message);
    }

}
