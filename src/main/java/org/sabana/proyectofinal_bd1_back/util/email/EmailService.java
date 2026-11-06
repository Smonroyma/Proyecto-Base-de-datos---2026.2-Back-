package org.sabana.proyectofinal_bd1_back.util.email;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;

import java.util.Map;

public interface EmailService {

    void sendOtpEmail(String to, String nombreUsuario, String codigoOtp, int minutosValidez);

    void sendNotificacionEmail(String to,
                               String tituloEncabezado,
                               String nombreDestinatario,
                               String mensajePrincipal,
                               Map<String, String> detalles,
                               String notaPie);

    void sendFacturaEmail(String to, FacturaResponseDTO factura);

    default void enviarCorreoOtp(String destinatario, String nombreUsuario, String codigoOtp, int minutosValidez) {
        sendOtpEmail(destinatario, nombreUsuario, codigoOtp, minutosValidez);
    }

}
