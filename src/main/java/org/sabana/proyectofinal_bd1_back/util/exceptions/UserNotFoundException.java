package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long idUsuario) {
        super(String.format("No se encontró el usuario con ID: %d", idUsuario));
    }

    public UserNotFoundException(String message) {
        super(message);
    }

}
