package org.sabana.proyectofinal_bd1_back.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para presentar la información de un producto al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos de un producto del catálogo")
public class ProductoResponseDTO {

    @Schema(description = "Identificador único del producto", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre del producto", example = "Café Americano")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Espresso doble diluido con agua caliente")
    private String descripcion;

    @Schema(description = "Precio unitario del producto", example = "6500.0")
    private Double precio;

    @Schema(description = "Indica si el producto está disponible para la venta", example = "true")
    private Boolean disponibilidad;

    @Schema(description = "Cantidad de existencias en inventario", example = "50")
    private Integer stock;

    @Schema(description = "URL de la imagen del producto", example = "https://imagenes.savanacoffee.com/americano.jpg")
    private String imagenUrl;

    @Schema(description = "Nombre de la categoría asociada (FK)", example = "Bebidas Calientes")
    private String nombreCategoria;

}
