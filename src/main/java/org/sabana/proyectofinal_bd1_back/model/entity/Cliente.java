package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Long idCliente;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;

    public String getNombreCompleto() {
        if (apellido == null || apellido.isBlank()) {
            return nombre != null ? nombre : "";
        }
        return (nombre != null ? nombre : "") + " " + apellido;
    }

}
