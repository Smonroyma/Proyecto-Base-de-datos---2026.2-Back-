package org.sabana.proyectofinal_bd1_back.util.email;

import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.sabana.proyectofinal_bd1_back.model.dto.PagoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailTemplateBuilder {

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

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin:22px 0;">
                          <tr>
                            <td align="center" style="background-color:#EDE0D4; border:1px solid #E6CCB2; border-radius:14px; padding:26px 20px;">
                              <span style="display:inline-block; background-color:#7F5539; color:#F4F0EC; border-radius:30px; padding:14px 34px; font-family:'Courier New',Courier,monospace; font-size:32px; font-weight:bold; letter-spacing:8px;">%s</span>
                            </td>
                          </tr>
                        </table>

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

    public String buildNotificacionGeneralTemplate(String tituloEncabezado,
                                                   String nombreDestinatario,
                                                   String mensajePrincipal,
                                                   Map<String, String> detalles,
                                                   String notaPie) {
        StringBuilder filasDetalle = new StringBuilder();
        if (detalles != null && !detalles.isEmpty()) {
            for (Map.Entry<String, String> entry : detalles.entrySet()) {
                filasDetalle.append("""
                    <tr>
                      <td style="padding:10px 14px; border-bottom:1px solid #E6CCB2; font-size:14px; color:#7F5539; font-weight:bold; width:40%%;">
                        %s
                      </td>
                      <td style="padding:10px 14px; border-bottom:1px solid #E6CCB2; font-size:14px; color:#432818;">
                        %s
                      </td>
                    </tr>
                """.formatted(entry.getKey(), entry.getValue()));
            }
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - Savana Coffee</title>
                </head>
                <body style="margin:0; padding:0; background-color:#F4F0EC; font-family:Georgia,'Times New Roman',serif; -webkit-font-smoothing:antialiased;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#F4F0EC; padding:30px 10px;">
                <tr><td align="center">

                  <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width:600px; background-color:#FFFFFF; border-radius:16px; overflow:hidden; border:1px solid #E6CCB2;">

                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:28px 20px; border-bottom:1px solid #E6CCB2;">
                        <div style="font-size:26px; letter-spacing:4px; color:#7F5539; text-transform:uppercase;">
                          Savana Coffee
                        </div>
                        <div style="font-size:12px; letter-spacing:3px; color:#9C6644; padding-top:6px; text-transform:uppercase;">
                          %s
                        </div>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:36px 35px 28px 35px; color:#432818; background-color:#F4F0EC;">
                        <h2 style="margin:0 0 22px 0; padding-bottom:8px; border-bottom:2px solid #B08968; color:#7F5539; font-size:24px; font-weight:normal;">
                          %s
                        </h2>

                        <p style="margin:0 0 16px 0; font-size:15px; line-height:1.6; color:#432818;">
                          Hola <strong>%s</strong>,
                        </p>
                        <p style="margin:0 0 20px 0; font-size:15px; line-height:1.6; color:#432818;">
                          %s
                        </p>

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#FFFFFF; border:1px solid #E6CCB2; border-radius:10px; margin:20px 0; overflow:hidden;">
                          %s
                        </table>

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#E6CCB2; border-left:4px solid #9C6644; border-radius:6px; margin:20px 0 10px 0;">
                          <tr>
                            <td style="padding:12px 16px; font-size:13px; line-height:1.5; color:#432818;">
                              %s
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:24px 20px; border-top:1px solid #E6CCB2;">
                        <p style="margin:0 0 6px 0; font-size:12px; color:#7F5539; font-weight:bold;">
                          Savana Coffee &copy; 2026. Todos los derechos reservados.
                        </p>
                        <p style="margin:0; font-size:11px; color:#9C6644; line-height:1.4;">
                          Correo automático generado por el sistema de gestión.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
                </table>
                </body>
                </html>
            """.formatted(
                tituloEncabezado,
                tituloEncabezado,
                tituloEncabezado,
                nombreDestinatario,
                mensajePrincipal,
                filasDetalle.toString(),
                notaPie
        );
    }

    public String buildFacturaEmailTemplate(FacturaResponseDTO factura) {
        StringBuilder filasPagos = new StringBuilder();
        if (factura.getPagos() != null && !factura.getPagos().isEmpty()) {
            for (PagoResponseDTO pago : factura.getPagos()) {
                String etiquetaMetodo = pago.getTipoMetodoPago() != null
                        ? pago.getTipoMetodoPago()
                        : "N/A";
                filasPagos.append("""
                    <tr>
                      <td style="padding:6px 0; font-size:13px; color:#7F5539;">
                        %s
                      </td>
                      <td align="right" style="padding:6px 0; font-size:13px; color:#432818; font-weight:bold;">
                        $%,.2f
                      </td>
                    </tr>
                """.formatted(etiquetaMetodo, pago.getMonto() != null ? pago.getMonto() : 0.0));
            }
        }

        String fechaStr = (factura.getFecha() != null)
                ? factura.getFecha().toString().replace("T", " ")
                : "Reciente";

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Comprobante de Pago %s - Savana Coffee</title>
                </head>
                <body style="margin:0; padding:0; background-color:#F4F0EC; font-family:Georgia,'Times New Roman',serif; -webkit-font-smoothing:antialiased;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#F4F0EC; padding:30px 10px;">
                <tr><td align="center">

                  <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width:600px; background-color:#FFFFFF; border-radius:16px; overflow:hidden; border:1px solid #E6CCB2;">

                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:28px 20px; border-bottom:1px solid #E6CCB2;">
                        <div style="font-size:26px; letter-spacing:4px; color:#7F5539; text-transform:uppercase;">
                          Savana Coffee
                        </div>
                        <div style="font-size:12px; letter-spacing:3px; color:#9C6644; padding-top:6px; text-transform:uppercase;">
                          Comprobante Electrónico de Pago
                        </div>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:32px 35px 28px 35px; color:#432818; background-color:#F4F0EC;">

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin-bottom:20px;">
                          <tr>
                            <td>
                              <span style="font-size:12px; text-transform:uppercase; letter-spacing:1px; color:#9C6644;">Factura No.</span>
                              <h2 style="margin:4px 0 0 0; color:#7F5539; font-size:22px;">%s</h2>
                            </td>
                            <td align="right" style="font-size:13px; color:#7F5539;">
                              <strong>Fecha:</strong> %s
                            </td>
                          </tr>
                        </table>

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#FFFFFF; border:1px solid #E6CCB2; border-radius:10px; padding:16px; margin-bottom:20px;">
                          <tr>
                            <td style="font-size:13px; color:#432818; line-height:1.6;">
                              <strong>Cliente:</strong> %s<br>
                              <strong>Contacto:</strong> %s
                            </td>
                            <td align="right" style="font-size:13px; color:#432818; line-height:1.6;">
                              <strong>Atendido por:</strong> %s<br>
                              <strong>Pedido ID:</strong> #%d
                            </td>
                          </tr>
                        </table>

                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#FFFFFF; border:1px solid #E6CCB2; border-radius:10px; padding:18px; margin-bottom:20px;">
                          <tr>
                            <td style="padding:6px 0; font-size:14px; color:#7F5539;">Subtotal:</td>
                            <td align="right" style="padding:6px 0; font-size:14px; color:#432818;">$%,.2f</td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; font-size:14px; color:#7F5539;">Impuesto IVA (19%%):</td>
                            <td align="right" style="padding:6px 0; font-size:14px; color:#432818;">$%,.2f</td>
                          </tr>
                          <tr>
                            <td style="padding:6px 0; font-size:14px; color:#7F5539;">Propina voluntaria:</td>
                            <td align="right" style="padding:6px 0; font-size:14px; color:#432818;">$%,.2f</td>
                          </tr>
                          <tr>
                            <td colspan="2" style="border-top:2px solid #E6CCB2; padding-top:10px; margin-top:6px;"></td>
                          </tr>
                          <tr>
                            <td style="font-size:18px; color:#7F5539; font-weight:bold;">TOTAL PAGADO:</td>
                            <td align="right" style="font-size:20px; color:#7F5539; font-weight:bold;">$%,.2f</td>
                          </tr>
                        </table>

                        <div style="font-size:13px; font-weight:bold; color:#7F5539; margin-bottom:8px; text-transform:uppercase; letter-spacing:1px;">
                          Detalle del Pago
                        </div>
                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color:#FFFFFF; border:1px solid #E6CCB2; border-radius:10px; padding:14px 18px; margin-bottom:22px;">
                          %s
                        </table>

                        <p style="margin:0; font-size:13px; line-height:1.5; color:#7F5539; text-align:center;">
                          ¡Gracias por tu compra en Savana Coffee! Esperamos verte pronto de nuevo.
                        </p>
                      </td>
                    </tr>

                    <tr>
                      <td align="center" style="background-color:#EDE0D4; padding:24px 20px; border-top:1px solid #E6CCB2;">
                        <p style="margin:0 0 6px 0; font-size:12px; color:#7F5539; font-weight:bold;">
                          Savana Coffee &copy; 2026. Todos los derechos reservados.
                        </p>
                        <p style="margin:0; font-size:11px; color:#9C6644; line-height:1.4;">
                          Documento generado electrónicamente. Consérvalo como comprobante de pago.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
                </table>
                </body>
                </html>
            """.formatted(
                factura.getNumFactura() != null ? factura.getNumFactura() : "S/N",
                factura.getNumFactura() != null ? factura.getNumFactura() : "S/N",
                fechaStr,
                factura.getNombreCliente() != null ? factura.getNombreCliente() : "Consumidor Final",
                factura.getTelefonoCliente() != null ? factura.getTelefonoCliente() : "N/A",
                factura.getNombreUsuario() != null ? factura.getNombreUsuario() : "Personal en turno",
                factura.getIdPedido() != null ? factura.getIdPedido() : 0,
                calcularSubtotalFactura(factura),
                factura.getImpuesto() != null ? factura.getImpuesto() : 0.0,
                factura.getPropina() != null ? factura.getPropina() : 0.0,
                factura.getTotal() != null ? factura.getTotal() : 0.0,
                filasPagos.toString()
        );
    }

    private double calcularSubtotalFactura(FacturaResponseDTO factura) {
        double total = factura.getTotal() != null ? factura.getTotal() : 0.0;
        double impuesto = factura.getImpuesto() != null ? factura.getImpuesto() : 0.0;
        double propina = factura.getPropina() != null ? factura.getPropina() : 0.0;
        return Math.round((total - impuesto - propina) * 100.0) / 100.0;
    }
}
