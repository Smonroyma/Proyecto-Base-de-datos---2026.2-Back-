package org.sabana.proyectofinal_bd1_back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoRequestDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.MetodoPagoResponseDTO;
import org.sabana.proyectofinal_bd1_back.service.MetodoPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administración de métodos de pago en Savana Data Coffee.
 */
@RestController
@RequestMapping("/api/metodos-pago")
@Tag(name = "Métodos de Pago", description = "Endpoints para la configuración y consulta de medios de pago habilitados en caja (EFECTIVO, TARJETA, TRANSFERENCIA)")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar métodos de pago",
            description = "Obtiene el catálogo de métodos de pago registrados. Permite filtrar opcionalmente solo aquellos activos."
    )
    public ResponseEntity<List<MetodoPagoResponseDTO>> obtenerMetodosPago(
            @Parameter(description = "Si es true, retorna únicamente los métodos de pago activos", example = "true")
            @RequestParam(name = "soloActivos", required = false) Boolean soloActivos) {
        List<MetodoPagoResponseDTO> metodos = metodoPagoService.obtenerMetodosPago(soloActivos);
        return ResponseEntity.ok(metodos);
    }

    @GetMapping("/{tipo}")
    @Operation(
            summary = "Consultar método de pago por tipo",
            description = "Retorna el detalle de un método de pago específico según su tipo (EFECTIVO, TARJETA, TRANSFERENCIA)."
    )
    public ResponseEntity<MetodoPagoResponseDTO> buscarPorTipo(
            @Parameter(description = "Tipo único del método de pago", example = "EFECTIVO", required = true)
            @PathVariable String tipo) {
        MetodoPagoResponseDTO metodo = metodoPagoService.buscarPorTipo(tipo);
        return ResponseEntity.ok(metodo);
    }

    @PostMapping
    @Operation(
            summary = "Registrar nuevo método de pago",
            description = "Crea un nuevo método de pago en el sistema validando la unicidad de su tipo."
    )
    public ResponseEntity<MetodoPagoResponseDTO> crear(
            @Valid @RequestBody MetodoPagoRequestDTO requestDTO) {
        MetodoPagoResponseDTO creado = metodoPagoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{tipo}")
    @Operation(
            summary = "Actualizar método de pago",
            description = "Actualiza los datos y estado de un método de pago existente."
    )
    public ResponseEntity<MetodoPagoResponseDTO> actualizar(
            @Parameter(description = "Tipo único del método de pago a actualizar", example = "EFECTIVO", required = true)
            @PathVariable String tipo,
            @Valid @RequestBody MetodoPagoRequestDTO requestDTO) {
        MetodoPagoResponseDTO actualizado = metodoPagoService.actualizar(tipo, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{tipo}")
    @Operation(
            summary = "Eliminar método de pago",
            description = "Elimina permanentemente un método de pago del catálogo según su tipo."
    )
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Tipo único del método de pago a eliminar", example = "EFECTIVO", required = true)
            @PathVariable String tipo) {
        metodoPagoService.eliminar(tipo);
        return ResponseEntity.noContent().build();
    }

}
