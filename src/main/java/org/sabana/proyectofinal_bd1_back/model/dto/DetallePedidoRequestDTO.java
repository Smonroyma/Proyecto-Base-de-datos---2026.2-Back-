package org.sabana.proyectofinal_bd1_back.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para cada ítem en el registro de un pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un ítem de producto dentro de la solicitud de pedido")
public class DetallePedidoRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @JsonAlias({"productoId", "id_producto"})
    @Schema(description = "Identificador único del producto a ordenar", example = "1")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    @Schema(description = "Cantidad solicitada del producto", example = "2")
    private Integer cantidad;

    public Long getProductoId() {
        return idProducto;
    }

    public void setProductoId(Long productoId) {
        this.idProducto = productoId;
    }

}
