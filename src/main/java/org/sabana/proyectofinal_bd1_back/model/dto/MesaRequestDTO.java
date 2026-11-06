package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de una mesa")
public class MesaRequestDTO {

    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser un entero positivo mayor o igual a 1")
    @JsonAlias({"numero", "idMesa"})
    @Schema(description = "Número identificador único de la mesa en el salón (PK)", example = "5")
    private Integer numeroMesa;

    @NotNull(message = "La capacidad de la mesa es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser de al menos 1 puesto")
    @Schema(description = "Capacidad de comensales sentados en la mesa", example = "4")
    private Integer capacidad;

    @NotBlank(message = "El estado de la mesa es obligatorio")
    @Pattern(
            regexp = "^(DISPONIBLE|OCUPADA|RESERVADA|EN_MANTENIMIENTO)$",
            message = "El estado de la mesa debe ser uno de: DISPONIBLE, OCUPADA, RESERVADA, EN_MANTENIMIENTO"
    )
    @Schema(description = "Estado operativo de la mesa", example = "DISPONIBLE", allowableValues = {"DISPONIBLE", "OCUPADA", "RESERVADA", "EN_MANTENIMIENTO"})
    private String estado;

}
