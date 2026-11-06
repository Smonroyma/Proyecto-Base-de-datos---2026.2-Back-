package org.sabana.proyectofinal_bd1_back.util.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.sabana.proyectofinal_bd1_back.model.dto.FacturaResponseDTO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder emailTemplateBuilder;

    @Value("${spring.mail.username:no-reply@savanacoffee.com}")
    private String remitente;

    public EmailServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            EmailTemplateBuilder emailTemplateBuilder) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.emailTemplateBuilder = emailTemplateBuilder;
    }

    @Async
    @Override
    public void sendOtpEmail(String to, String nombreUsuario, String codigoOtp, int minutosValidez) {
        if (to == null || to.isBlank()) {
            log.warn("No se proporcionó un destinatario válido para el envío de código OTP al usuario: {}", nombreUsuario);
            return;
        }

        String contenidoHtml = emailTemplateBuilder.buildOtpEmailTemplate(nombreUsuario, codigoOtp, minutosValidez);
        String asunto = "☕ Tu código de verificación 2FA - Savana Data Coffee";

        if (mailSender == null) {
            log.warn("JavaMailSender no está disponible en este entorno. " +
                    "Código OTP generado: [{}] para el usuario: {} ({}) con vigencia de {} minutos.",
                    codigoOtp, nombreUsuario, to, minutosValidez);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Correo 2FA/OTP enviado exitosamente a: {}", to);
        } catch (MessagingException | RuntimeException ex) {
            log.warn("No se pudo despachar el correo OTP a {} vía SMTP (modo desarrollo/offline activo). " +
                    "Código OTP generado: [{}] para el usuario: {}. Causa: {}",
                    to, codigoOtp, nombreUsuario, ex.getMessage());
        }
    }

    @Async
    @Override
    public void sendNotificacionEmail(String to,
                                      String tituloEncabezado,
                                      String nombreDestinatario,
                                      String mensajePrincipal,
                                      Map<String, String> detalles,
                                      String notaPie) {
        if (to == null || to.isBlank()) {
            log.warn("No se proporcionó un correo electrónico válido para enviar la notificación '{}' a {}",
                    tituloEncabezado, nombreDestinatario);
            return;
        }

        String contenidoHtml = emailTemplateBuilder.buildNotificacionGeneralTemplate(
                tituloEncabezado,
                nombreDestinatario,
                mensajePrincipal,
                detalles,
                notaPie
        );
        String asunto = String.format("☕ %s - Savana Data Coffee", tituloEncabezado);

        if (mailSender == null) {
            log.warn("JavaMailSender no está disponible en este entorno. " +
                    "Notificación '{}' para {} ({}) registrada en consola: {}",
                    tituloEncabezado, nombreDestinatario, to, detalles);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Correo de notificación '{}' enviado exitosamente a: {}", tituloEncabezado, to);
        } catch (MessagingException | RuntimeException ex) {
            log.warn("No se pudo despachar la notificación '{}' a {} vía SMTP (modo desarrollo/offline activo). " +
                    "Detalles: {}. Causa: {}",
                    tituloEncabezado, to, detalles, ex.getMessage());
        }
    }

    @Async
    @Override
    public void sendFacturaEmail(String to, FacturaResponseDTO factura) {
        if (to == null || to.isBlank() || factura == null) {
            log.warn("No se pudo enviar el comprobante de factura por correo: destinatario o factura nulos.");
            return;
        }

        String contenidoHtml = emailTemplateBuilder.buildFacturaEmailTemplate(factura);
        String numeroFactura = factura.getNumFactura() != null ? factura.getNumFactura() : "S/N";
        String asunto = String.format("☕ Comprobante de Pago %s - Savana Data Coffee", numeroFactura);

        if (mailSender == null) {
            log.warn("JavaMailSender no está disponible en este entorno. " +
                    "Factura electrónica [{}] por total de ${} emitida para {} ({}).",
                    numeroFactura, factura.getTotal(), factura.getNombreCliente(), to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Comprobante electrónico de factura {} enviado exitosamente a: {}", numeroFactura, to);
        } catch (MessagingException | RuntimeException ex) {
            log.warn("No se pudo despachar el comprobante de factura {} a {} vía SMTP (modo desarrollo/offline activo). " +
                    "Total pagado: ${}. Causa: {}",
                    numeroFactura, to, factura.getTotal(), ex.getMessage());
        }
    }

    @Async
    @Override
    public void enviarCorreoOtp(String destinatario, String nombreUsuario, String codigoOtp, int minutosValidez) {
        sendOtpEmail(destinatario, nombreUsuario, codigoOtp, minutosValidez);
    }

}
