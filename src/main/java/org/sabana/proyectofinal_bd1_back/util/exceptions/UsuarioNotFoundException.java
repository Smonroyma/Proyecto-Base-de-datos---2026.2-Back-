package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class UsuarioNotFoundException extends UserNotFoundException {

    public UsuarioNotFoundException(Long idUsuario) {
        super(idUsuario);
    }

    public UsuarioNotFoundException(String message) {
        super(message);
    }

}
