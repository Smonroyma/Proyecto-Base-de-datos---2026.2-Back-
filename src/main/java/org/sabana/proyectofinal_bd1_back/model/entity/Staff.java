package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Entidad de dominio Staff.
 * POJO puro sin anotaciones de persistencia JPA.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private Boolean dosFacHab;
    private LocalDate fechaInicio;

    /**
     * Rol resuelto según la tabla hija (ADMINISTRADOR o MESERO).
     */
    private String rol;

    public String getNombreCompleto() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }

}
