package org.sabana.proyectofinal_bd1_back.util.email;

/**
 * Servicio para el envío de correos electrónicos transaccionales y notificaciones de seguridad.
 */
public interface EmailService {

    void enviarCorreoOtp(String destinatario, String nombreUsuario, String codigoOtp, int minutosValidez);

}
