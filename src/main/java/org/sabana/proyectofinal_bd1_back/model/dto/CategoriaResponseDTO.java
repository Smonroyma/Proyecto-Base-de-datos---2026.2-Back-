package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos de una categoría del catálogo")
public class CategoriaResponseDTO {

    @Schema(description = "Nombre único de la categoría (PK)", example = "Bebidas Calientes")
    private String nombreCategoria;

    @Schema(description = "Estado de la categoría en el catálogo", example = "ACTIVA")
    private String estado;

    @Schema(description = "Descripción de la categoría", example = "Cafés espresso, lattes, infusiones y tés calientes")
    private String descripcion;

}
