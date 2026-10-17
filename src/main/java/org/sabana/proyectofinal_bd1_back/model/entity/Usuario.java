package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Usuario.
 * Mapea directamente la tabla 'usuario' de PostgreSQL sin anotaciones JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    private String nombre;
    private String email;
    private String passwordHash;
    private String rol; // ADMINISTRADOR, BARISTA, CAJERO
    private Boolean activo;
    private Boolean dosFactoresHabilitado;
    private String codigoOtp;
    private LocalDateTime expiracionOtp;
    private LocalDateTime fechaRegistro;

}
