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
                    <title>Código de Verificación - Savana Coffee</title>
                </head>
                <body style="margin:0; padding:0; background-color:#F4F0EC; font-family:Georgia,'Times New Roman',serif; -webkit-font-smoothing:antialiased;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#F4F0EC; padding:30px 10px;">
                <tr><td align="center">
                
                  <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width:600px; background-color:#FFFFFF; border-radius:16px; overflow:hidden; border:1px solid #E6CCB2;">
                
                    <!-- Barra superior (estilo navbar) -->
                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:28px 20px; border-bottom:1px solid #E6CCB2;">
                        <div style="font-size:26px; letter-spacing:4px; color:#7F5539; text-transform:uppercase;">
                          Savana Coffee
                        </div>
                        <div style="font-size:12px; letter-spacing:3px; color:#9C6644; padding-top:6px; text-transform:uppercase;">
                          Verificación de seguridad
                        </div>
                      </td>
                    </tr>
                
                    <!-- Cuerpo -->
                    <tr>
                      <td style="padding:36px 35px 28px 35px; color:#432818; background-color:#F4F0EC;">
                
                        <h2 style="margin:0 0 22px 0; padding-bottom:8px; border-bottom:2px solid #B08968; color:#7F5539; font-size:24px; font-weight:normal;">
                          Verificación en dos pasos
                        </h2>
                
                        <p style="margin:0 0 18px 0; font-size:15px; line-height:1.6; color:#432818;">
                          Hola <strong>%s</strong>,
                        </p>
                        <p style="margin:0 0 10px 0; font-size:15px; line-height:1.6; color:#432818;">
                          Has iniciado sesión en <strong>Savana Coffee</strong>. Para completar el acceso, ingresa este código de verificación:
                        </p>
                
                        <!-- Tarjeta con código OTP -->
                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin:22px 0;">
                          <tr>
                            <td align="center" style="background-color:#EDE0D4; border:1px solid #E6CCB2; border-radius:14px; padding:26px 20px;">
                              <span style="display:inline-block; background-color:#7F5539; color:#F4F0EC; border-radius:30px; padding:14px 34px; font-family:'Courier New',Courier,monospace; font-size:32px; font-weight:bold; letter-spacing:8px;">%s</span>
                            </td>
                          </tr>
                        </table>
                
                        <!-- Aviso de vigencia -->
                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#E6CCB2; border-left:4px solid #9C6644; border-radius:6px; margin:0 0 24px 0;">
                          <tr>
                            <td style="padding:12px 16px; font-size:13px; line-height:1.5; color:#432818;">
                              Este código es de un solo uso y vence en <strong>%d minutos</strong>.
                            </td>
                          </tr>
                        </table>
                
                        <p style="margin:0; font-size:13px; line-height:1.5; color:#7F5539;">
                          Si no solicitaste este código, alguien podría estar intentando acceder a tu cuenta. Cambia tu contraseña de inmediato o avisa a la administración.
                        </p>
                      </td>
                    </tr>
                
                    <!-- Pie (estilo footer) -->
                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:24px 20px; border-top:1px solid #E6CCB2;">
                        <p style="margin:0 0 6px 0; font-size:12px; color:#7F5539; font-weight:bold;">
                          Savana Coffee &copy; 2026. Todos los derechos reservados.
                        </p>
                        <p style="margin:0; font-size:11px; color:#9C6644; line-height:1.4;">
                          Correo automático, por favor no respondas a este mensaje.
                        </p>
                      </td>
                    </tr>
                
                  </table>
                </td></tr>
                </table>
                </body>
                </html>
            """.formatted(nombreUsuario, codigoOtp, minutosValidez);
    }

}
