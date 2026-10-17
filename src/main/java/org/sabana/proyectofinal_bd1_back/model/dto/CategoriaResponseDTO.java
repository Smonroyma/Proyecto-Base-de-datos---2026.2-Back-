package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para presentar la información de una categoría al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos de una categoría del catálogo")
public class CategoriaResponseDTO {

    @Schema(description = "Identificador único de la categoría", example = "1")
    private Long idCategoria;

    @Schema(description = "Nombre de la categoría", example = "Bebidas Calientes")
    private String nombre;

    @Schema(description = "Descripción de la categoría", example = "Cafés espresso, lattes, infusiones y tés calientes")
    private String descripcion;

    @Schema(description = "Indica si la categoría está activa para ventas", example = "true")
    private Boolean activo;

}
