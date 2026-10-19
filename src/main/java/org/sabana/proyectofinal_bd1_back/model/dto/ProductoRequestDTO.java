package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación y actualización de productos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro o actualización de un producto en el catálogo")
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre del producto no puede superar los 100 caracteres")
    @Schema(description = "Nombre único del producto", example = "Café Americano")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    @Schema(description = "Descripción detallada del producto", example = "Espresso doble diluido con agua caliente")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo mayor a cero")
    @Schema(description = "Precio unitario del producto", example = "6500.0")
    private Double precio;

    @NotNull(message = "La disponibilidad es obligatoria")
    @JsonAlias({"disponible"})
    @Schema(description = "Indica si el producto está disponible para la venta", example = "true")
    private Boolean disponibilidad;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad de existencias en inventario", example = "50")
    private Integer stock;

    @NotBlank(message = "La URL de la imagen del producto es obligatoria")
    @Size(max = 255, message = "La URL de la imagen no puede superar los 255 caracteres")
    @Schema(description = "URL de la imagen representativa del producto", example = "https://imagenes.savanacoffee.com/americano.jpg")
    private String imagenUrl;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @JsonAlias({"categoria", "idCategoria"})
    @Schema(description = "Nombre de la categoría a la que pertenece el producto (FK)", example = "Bebidas Calientes")
    private String nombreCategoria;

}
