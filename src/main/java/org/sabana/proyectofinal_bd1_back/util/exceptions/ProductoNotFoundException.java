package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class ProductoNotFoundException extends ResourceNotFoundException {

    public ProductoNotFoundException(Long idProducto) {
        super(String.format("No se encontró el producto con ID: %d", idProducto));
    }

    public ProductoNotFoundException(String message) {
        super(message);
    }

}
