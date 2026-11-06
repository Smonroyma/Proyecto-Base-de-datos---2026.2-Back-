package org.sabana.proyectofinal_bd1_back.util.exceptions;

public class MesaNotFoundException extends ResourceNotFoundException {

    public MesaNotFoundException(Integer numeroMesa) {
        super(String.format("No se encontró la mesa con número: %d", numeroMesa));
    }

    public MesaNotFoundException(Long idMesa) {
        super(String.format("No se encontró la mesa con número: %d", idMesa));
    }

    public MesaNotFoundException(String message) {
        super(message);
    }

}
