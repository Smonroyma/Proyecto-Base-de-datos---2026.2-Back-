package org.sabana.proyectofinal_bd1_back.util.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estructura estándar de respuesta para errores de la API")
public class ErrorResponseDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Marca de tiempo en la que se generó el error", example = "2026-10-16T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Nombre descriptivo del estado HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Mensaje explicativo del error", example = "No se encontró el cliente con ID: 1")
    private String message;

    @Schema(description = "Ruta de la solicitud que originó el error", example = "/api/clientes/1")
    private String path;

    @Schema(description = "Detalles específicos de errores de validación por campo")
    private Map<String, String> validationErrors;

}
