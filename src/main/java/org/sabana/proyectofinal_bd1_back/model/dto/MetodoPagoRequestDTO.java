package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación y actualización de un método de pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para registrar o actualizar un método de pago")
public class MetodoPagoRequestDTO {

    @NotBlank(message = "El tipo del método de pago es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder los 50 caracteres")
    @JsonAlias({"codigo"})
    @Schema(description = "Tipo único identificador del método de pago (PK)", example = "EFECTIVO", allowableValues = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"})
    private String tipo;

    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Schema(description = "Nombre visible del método de pago", example = "Efectivo")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción u observaciones operativas del medio de pago", example = "Pago en efectivo en caja")
    private String descripcion;

    @Schema(description = "Indica si el método de pago se encuentra habilitado para facturación", example = "true")
    private Boolean activo;

}
