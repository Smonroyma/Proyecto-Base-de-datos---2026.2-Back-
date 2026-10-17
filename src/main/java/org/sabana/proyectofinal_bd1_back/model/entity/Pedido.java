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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long idPedido;
    private Long idCliente;
    private String nombreCliente;
    private Long idMesa;
    private Integer numeroMesa;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaHora;
    private String estado;
    private String tipoPedido;
    private Double total;
    private String observaciones;

    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

}
