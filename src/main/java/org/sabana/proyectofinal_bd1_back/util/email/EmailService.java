package org.sabana.proyectofinal_bd1_back.util.email;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;

import java.util.Map;

/**
 * Contrato del servicio de notificaciones transaccionales por correo electrónico con plantillas HTML.
 */
public interface EmailService {

    /**
     * Envía de forma asíncrona el código de verificación en dos pasos (2FA / OTP).
     */
    void sendOtpEmail(String to, String nombreUsuario, String codigoOtp, int minutosValidez);

    /**
     * Envía de forma asíncrona una notificación general (Reservas, Cuentas de Usuario/Cliente, etc.).
     */
    void sendNotificacionEmail(String to,
                               String tituloEncabezado,
                               String nombreDestinatario,
                               String mensajePrincipal,
                               Map<String, String> detalles,
                               String notaPie);

    /**
     * Envía de forma asíncrona el comprobante electrónico de facturación y pago.
     */
    void sendFacturaEmail(String to, FacturaResponseDTO factura);

    /**
     * Alias de compatibilidad para el envío de código OTP.
     */
    default void enviarCorreoOtp(String destinatario, String nombreUsuario, String codigoOtp, int minutosValidez) {
        sendOtpEmail(destinatario, nombreUsuario, codigoOtp, minutosValidez);
    }

}
