package org.sabana.proyectofinal_bd1_back.util;

import org.sabana.proyectofinal_bd1_back.model.entity.DetallePedido;
import org.sabana.proyectofinal_bd1_back.model.entity.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Utilidad transversal para el cálculo dinámico del subtotal de un pedido.
 * Regla V3: el subtotal NO se persiste en pedido ni en detalle_pedido; se obtiene
 * sumando (cantidad * precio) de cada producto contenido en el detalle del pedido.
 */
public final class SubtotalCalculator {

    private SubtotalCalculator() {
    }

    /**
     * Calcula el subtotal de un ítem de detalle (cantidad * precio unitario del producto).
     */
    public static double calcularSubtotalItem(DetallePedido detalle) {
        if (detalle == null || detalle.getCantidad() == null) {
            return 0.0;
        }
        Double precio = detalle.getPrecio();
        if (precio == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(precio)
                .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calcula el subtotal de una lista de detalles sumando cada (cantidad * precio).
     */
    public static double calcularSubtotal(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido d : detalles) {
            total = total.add(BigDecimal.valueOf(calcularSubtotalItem(d)));
        }
        return total.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calcula el subtotal de un pedido a partir de sus detalles.
     */
    public static double calcularSubtotal(Pedido pedido) {
        return pedido == null ? 0.0 : calcularSubtotal(pedido.getDetalles());
    }
}
