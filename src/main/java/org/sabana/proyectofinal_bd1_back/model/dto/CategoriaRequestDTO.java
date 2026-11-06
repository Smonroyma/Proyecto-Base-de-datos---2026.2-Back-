package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de una categoría de productos")
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder los 100 caracteres")
    @JsonAlias({"nombre"})
    @Schema(description = "Nombre único de la categoría (PK)", example = "Bebidas Calientes")
    private String nombreCategoria;

    @NotBlank(message = "El estado de la categoría es obligatorio")
    @Pattern(regexp = "^(ACTIVA|INACTIVA)$", message = "El estado de la categoría debe ser ACTIVA o INACTIVA")
    @Schema(description = "Estado de la categoría en el catálogo", example = "ACTIVA", allowableValues = {"ACTIVA", "INACTIVA"})
    private String estado;

    @NotBlank(message = "La descripción de la categoría es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción detallada del tipo de productos de la categoría", example = "Cafés espresso, lattes, infusiones y tés calientes")
    private String descripcion;

}
