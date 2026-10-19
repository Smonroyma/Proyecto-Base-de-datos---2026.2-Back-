package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio Pedido.
 * Mapea directamente la tabla de base de datos 'pedido' sin anotaciones JPA.
 * Regla V2: Prohibido pedidos para llevar; todo pedido exige obligatoriamente numeroMesa y maneja subtotal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long idPedido;
    private Long idCliente;
    private String nombreCliente;
    private Integer numeroMesa;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fecha;
    private String estado;
    private Double subtotal;
    private String observaciones;

    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

}
