package org.sabana.proyectofinal_bd1_back.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long idPedido;
    private LocalDateTime fecha;
    private String estado;
    private String observaciones;

    private Long idCliente;
    private String nombreCliente;
    private Integer numeroMesa;
    private Long idUsuario;
    private String nombreUsuario;

    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

}
