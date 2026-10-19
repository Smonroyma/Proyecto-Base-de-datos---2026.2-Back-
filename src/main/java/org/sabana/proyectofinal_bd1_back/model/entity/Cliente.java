package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Cliente.
 * Mapea directamente la tabla cliente de la base de datos sin anotaciones JPA.
 * Regla de negocio: El cliente no tiene cuenta ni contraseña y siempre se guarda permanentemente en la BD.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Long idCliente;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private LocalDateTime fechaRegistro;

    public String getNombreCompleto() {
        if (apellidos == null || apellidos.isBlank()) {
            return nombres != null ? nombres : "";
        }
        return (nombres != null ? nombres : "") + " " + apellidos;
    }

}
