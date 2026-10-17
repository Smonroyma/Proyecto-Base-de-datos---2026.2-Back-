package org.sabana.proyectofinal_bd1_back.util.exceptions;

/**
 * Excepción lanzada cuando un usuario/empleado solicitado no existe en el sistema.
 * Extiende de UserNotFoundException para compatibilidad con todas las convenciones.
 */
public class UsuarioNotFoundException extends UserNotFoundException {

    public UsuarioNotFoundException(Long idUsuario) {
        super(idUsuario);
    }

    public UsuarioNotFoundException(String message) {
        super(message);
    }

}
