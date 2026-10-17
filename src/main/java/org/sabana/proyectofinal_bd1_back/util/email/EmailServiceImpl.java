package org.sabana.proyectofinal_bd1_back.util.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementación de EmailService utilizando JavaMailSender y plantillas HTML multiparte.
 * Incluye fallback automático a consola cuando el servidor SMTP o JavaMailSender no están disponibles.
 */
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

    @Override
    public void enviarCorreoOtp(String destinatario, String nombreUsuario, String codigoOtp, int minutosValidez) {
        String contenidoHtml = emailTemplateBuilder.buildOtpEmailTemplate(nombreUsuario, codigoOtp, minutosValidez);
        String asunto = "☕ Tu código de verificación 2FA - Savana Data Coffee";

        if (mailSender == null) {
            log.warn("JavaMailSender no está disponible en este entorno. " +
                    "Código OTP generado: [{}] para el usuario: {} ({}) con vigencia de {} minutos.",
                    codigoOtp, nombreUsuario, destinatario, minutosValidez);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Correo 2FA/OTP enviado exitosamente a: {}", destinatario);
        } catch (MessagingException | RuntimeException ex) {
            log.warn("No se pudo despachar el correo a {} vía SMTP (modo desarrollo/offline activo). " +
                    "Código OTP generado: [{}] para el usuario: {}. Causa: {}",
                    destinatario, codigoOtp, nombreUsuario, ex.getMessage());
        }
    }

}
