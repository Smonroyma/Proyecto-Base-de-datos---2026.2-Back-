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
 * DTO de entrada para la creación y actualización de categorías de productos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de una categoría de productos")
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder los 100 caracteres")
    @Schema(description = "Nombre único de la categoría", example = "Bebidas Calientes")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción detallada del tipo de productos de la categoría", example = "Cafés espresso, lattes, infusiones y tés calientes")
    private String descripcion;

    @NotNull(message = "El estado activo es obligatorio")
    @Schema(description = "Indica si la categoría se encuentra activa en el catálogo de ventas", example = "true")
    private Boolean activo;

}
