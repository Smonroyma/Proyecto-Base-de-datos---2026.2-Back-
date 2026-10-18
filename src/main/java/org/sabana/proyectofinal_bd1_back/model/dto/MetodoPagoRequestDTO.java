package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El código del método de pago es obligatorio")
    @Size(max = 50, message = "El código no puede exceder los 50 caracteres")
    @Schema(description = "Código único identificador del método de pago", example = "TARJETA_CREDITO")
    private String codigo;

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Schema(description = "Nombre visible del método de pago", example = "Tarjeta de Crédito")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción u observaciones operativas del medio de pago", example = "Pagos con datáfono Visa, Mastercard o American Express")
    private String descripcion;

    @NotNull(message = "El estado activo es obligatorio")
    @Schema(description = "Indica si el método de pago se encuentra habilitado para facturación", example = "true")
    private Boolean activo;

}
