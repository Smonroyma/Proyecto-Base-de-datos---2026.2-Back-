package org.sabana.proyectofinal_bd1_back.util.email;

import org.springframework.stereotype.Component;

/**
 * Generador de plantillas HTML para correos electrónicos corporativos de Savana Data Coffee.
 * Utiliza estilos en línea (inline CSS) para asegurar compatibilidad total en clientes de correo.
 */
@Component
public class EmailTemplateBuilder {

    /**
     * Construye la plantilla HTML para el código de verificación en dos pasos (2FA / OTP).
     *
     * @param nombreUsuario Nombre del usuario destinatario
     * @param codigoOtp     Código numérico de 6 dígitos
     * @param minutosValidez Minutos de vigencia del código
     * @return Cadena con el documento HTML listo para enviar
     */
    public String buildOtpEmailTemplate(String nombreUsuario, String codigoOtp, int minutosValidez) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Código de Verificación - Savana Data Coffee</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #F5F5F5; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="table-layout: fixed; background-color: #F5F5F5; padding: 30px 10px;">
                    <tr>
                        <td align="center">
                            <!-- Contenedor Principal -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #E0E0E0;">
                                
                                <!-- Encabezado Corporativo -->
                                <tr>
                                    <td align="center" style="background: linear-gradient(135deg, #3E2723 0%%, #4E342E 100%%); background-color: #3E2723; padding: 35px 20px;">
                                        <table border="0" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="font-size: 34px; line-height: 1;">☕</td>
                                            </tr>
                                            <tr>
                                                <td align="center" style="color: #ECEFF1; font-size: 22px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; padding-top: 10px;">
                                                    SAVANA DATA COFFEE
                                                </td>
                                            </tr>
                                            <tr>
                                                <td align="center" style="color: #D7CCC8; font-size: 13px; letter-spacing: 1px; padding-top: 4px;">
                                                    Sistema de Seguridad & Autenticación
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>

                                <!-- Cuerpo del Mensaje -->
                                <tr>
                                    <td style="padding: 40px 35px 30px 35px; color: #333333;">
                                        <h2 style="margin: 0 0 16px 0; color: #3E2723; font-size: 22px; font-weight: 600;">
                                            Verificación en Dos Pasos (2FA)
                                        </h2>
                                        <p style="margin: 0 0 20px 0; font-size: 15px; line-height: 1.6; color: #555555;">
                                            Hola <strong>%s</strong>,
                                        </p>
                                        <p style="margin: 0 0 25px 0; font-size: 15px; line-height: 1.6; color: #555555;">
                                            Has iniciado sesión en la plataforma de <strong>Savana Data Coffee</strong>. Para completar el acceso de forma segura, ingresa el siguiente código de verificación temporal:
                                        </p>

                                        <!-- Caja Destacada para Código OTP -->
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin: 25px 0;">
                                            <tr>
                                                <td align="center">
                                                    <div style="background-color: #EFEBE9; border: 2px dashed #8D6E63; border-radius: 8px; padding: 18px 30px; display: inline-block;">
                                                        <span style="font-family: 'Courier New', Courier, monospace; font-size: 34px; font-weight: bold; letter-spacing: 8px; color: #3E2723;">
                                                            %s
                                                        </span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Advertencia de Caducidad -->
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #FFF8E1; border-left: 4px solid #FFB300; border-radius: 4px; margin: 20px 0 25px 0;">
                                            <tr>
                                                <td style="padding: 12px 16px; font-size: 13px; color: #6D4C41; line-height: 1.5;">
                                                    ⏱️ Este código es de uso único y tiene una vigencia estricta de <strong>%d minutos</strong>.
                                                </td>
                                            </tr>
                                        </table>

                                        <p style="margin: 0; font-size: 13px; line-height: 1.5; color: #777777;">
                                            Si tú no solicitaste este código, es posible que alguien esté intentando acceder a tu cuenta. Te recomendamos cambiar tu contraseña de inmediato o notificar a la administración del sistema.
                                        </p>
                                    </td>
                                </tr>

                                <!-- Pie de Página Institucional -->
                                <tr>
                                    <td align="center" style="background-color: #ECEFF1; padding: 25px 20px; border-top: 1px solid #CFD8DC;">
                                        <p style="margin: 0 0 6px 0; font-size: 12px; color: #546E7A; font-weight: bold;">
                                            Savana Data Coffee &copy; 2026. Todos los derechos reservados.
                                        </p>
                                        <p style="margin: 0; font-size: 11px; color: #78909C; line-height: 1.4;">
                                            Este es un correo automático generado por el servidor de seguridad. Por favor, no respondas a este mensaje.
                                        </p>
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(nombreUsuario, codigoOtp, minutosValidez);
    }

}
