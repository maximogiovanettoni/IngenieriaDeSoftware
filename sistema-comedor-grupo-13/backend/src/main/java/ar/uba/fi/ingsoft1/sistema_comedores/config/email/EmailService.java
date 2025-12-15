package ar.uba.fi.ingsoft1.sistema_comedores.config.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import ar.uba.fi.ingsoft1.sistema_comedores.config.email.exception.FailedToSendEmailException;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private EmailConfig emailConfig;

    private static final String VERIFICATION_EMAIL_SUBJECT = "Verificación de Cuenta - Sistema Comedor";
    private static final String RESEND_VERIFICATION_EMAIL_SUBJECT = "Nuevo Enlace de Verificación - Sistema Comedor";
    private static final String RESET_PASSWORD_EMAIL_SUBJECT = "Restablecimiento de Contraseña - Sistema Comedor";
    private static final String STAFF_CREATION_EMAIL_SUBJECT = "Cuenta de Personal Creada - Sistema Comedor";
    
    /**
     * Sends a verification email to the user
     * @param toEmail The recipient's email address
     * @param username The user's username
     * @param verificationToken The unique verification token
     */
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply" + ValidationConsts.ALLOWED_EMAIL_DOMAIN);
            helper.setTo(toEmail);
            helper.setSubject(VERIFICATION_EMAIL_SUBJECT);
            
            String verificationUrl = emailConfig.getVerificationUrl(verificationToken);

            String emailBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .button { 
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #007bff;
                            color: #ffffff !important;
                            text-decoration: none;
                            border-radius: 4px;
                            margin: 20px 0;
                        }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Hola %s,</h2>
                        <p>Bienvenido al Sistema de Comedor Universitario.</p>
                        <p>Para activar tu cuenta, por favor haz clic en el siguiente botón:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Activar mi cuenta</a>
                        </p>
                        <p>O copia y pega este enlace en tu navegador:</p>
                        <p><a href="%s">%s</a></p>
                        <div class="footer">
                            <p><strong>Este enlace expirará en 24 horas por motivos de seguridad.</strong></p>
                            <p>Si no solicitaste esta cuenta, puedes ignorar este correo.</p>
                            <p>Saludos,<br>Equipo del Sistema de Comedor Universitario</p>
                        </div>
                    </div>
                </body>
                </html>
                """, username, verificationUrl, verificationUrl, verificationUrl);

            helper.setText(emailBody, true); // El 'true' indica que es HTML
            
            emailSender.send(message);
            
           log.debug("=== EMAIL SENT ===");
           log.debug("To: " + toEmail);
           log.debug("Token: " + verificationToken);
           log.debug("Verification URL: " + verificationUrl);
           log.debug("==================");
            
        } catch (MessagingException e) {
           log.error("=== EMAIL ERROR ===");
           log.error("Error sending email to " + toEmail);
           log.error("Error type: " + e.getClass().getSimpleName());
           log.error("Error message: " + e.getMessage());
            if (e.getCause() != null) {
               log.error("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
           log.error("===================");
            throw new FailedToSendEmailException(VERIFICATION_EMAIL_SUBJECT, e);
        }
    }
    
    /**
     * Sends a resend verification email
     * @param toEmail The recipient's email address
     * @param username The user's username
     * @param verificationToken The new verification token
     */
    public void sendResendVerificationEmail(String toEmail, String username, String verificationToken) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply" + ValidationConsts.ALLOWED_EMAIL_DOMAIN);
            helper.setTo(toEmail);
            helper.setSubject(RESEND_VERIFICATION_EMAIL_SUBJECT);
            
            String verificationUrl = emailConfig.getVerificationUrl(verificationToken);
            
            String emailBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .button { 
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #007bff;
                            color: #ffffff !important;
                            text-decoration: none;
                            border-radius: 4px;
                            margin: 20px 0;
                        }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Hola %s,</h2>
                        <p>Has solicitado un nuevo enlace de verificación para tu cuenta.</p>
                        <p>Para activar tu cuenta, por favor haz clic en el siguiente botón:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Activar mi cuenta</a>
                        </p>
                        <p>O copia y pega este enlace en tu navegador:</p>
                        <p><a href="%s">%s</a></p>
                        <div class="footer">
                            <p><strong>Este nuevo enlace expirará en 24 horas por motivos de seguridad.</strong></p>
                            <p>Si no solicitaste este reenvío, puedes ignorar este correo.</p>
                            <p>Saludos,<br>Equipo del Sistema de Comedor Universitario</p>
                        </div>
                    </div>
                </body>
                </html>
                """, username, verificationUrl, verificationUrl, verificationUrl);
            
            helper.setText(emailBody, true); // El 'true' indica que es HTML
            
            emailSender.send(message);
            
           log.debug("=== RESEND EMAIL SENT ===");
           log.debug("To: " + toEmail);
           log.debug("Token: " + verificationToken);
           log.debug("Verification URL: " + verificationUrl);
           log.debug("=========================");
            
        } catch (MessagingException e) {
           log.error("=== RESEND EMAIL ERROR ===");
           log.error("Error sending resend email to " + toEmail);
           log.error("Error type: " + e.getClass().getSimpleName());
           log.error("Error message: " + e.getMessage());
            if (e.getCause() != null) {
               log.error("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
           log.error("===========================");
            throw new FailedToSendEmailException(RESEND_VERIFICATION_EMAIL_SUBJECT, e);
        }
    }

    /**
     * Sends a verification email to the user
     * @param toEmail The recipient's email address
     * @param username The user's username
     * @param resetToken The unique verification token
     */
    public void sendResetPasswordEmail(String toEmail, String username, String resetToken) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply" + ValidationConsts.ALLOWED_EMAIL_DOMAIN);
            helper.setTo(toEmail);
            helper.setSubject("Restablecimiento de Contraseña - Sistema Comedor");
            
            String resetURL = emailConfig.getResetPasswordUrl(resetToken);

            String emailBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .button { 
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #007bff;
                            color: #ffffff !important;
                            text-decoration: none;
                            border-radius: 4px;
                            margin: 20px 0;
                        }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Hola %s,</h2>
                        <p>Bienvenido al Sistema de Comedor Universitario.</p>
                        <p>Para restablecer su contraseña, por favor haz clic en el siguiente botón:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Restablecer mi contraseña</a>
                        </p>
                        <p>O copia y pega este enlace en tu navegador:</p>
                        <p><a href="%s">%s</a></p>
                        <div class="footer">
                            <p><strong>Este enlace expirará en 24 horas por motivos de seguridad.</strong></p>
                            <p>Si no solicitaste restablecer tu contraseña, puedes ignorar este correo.</p>
                            <p>Saludos,<br>Equipo del Sistema de Comedor Universitario</p>
                        </div>
                    </div>
                </body>
                </html>
                """, username, resetURL, resetURL, resetURL);

            helper.setText(emailBody, true); // El 'true' indica que es HTML
            
            emailSender.send(message);
            
           log.debug("=== EMAIL SENT ===");
           log.debug("To: " + toEmail);
           log.debug("Token: " + resetToken);
           log.debug("Reset URL: " + resetURL);
           log.debug("==================");
            
        } catch (MessagingException e) {
           log.error("=== EMAIL ERROR ===");
           log.error("Error sending email to " + toEmail);
           log.error("Error type: " + e.getClass().getSimpleName());
           log.error("Error message: " + e.getMessage());
            if (e.getCause() != null) {
               log.error("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
           log.error("===================");
            throw new FailedToSendEmailException(RESET_PASSWORD_EMAIL_SUBJECT, e);
        }
    }

    public void sendStaffCreationEmail(String toEmail, String temporaryPassword) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply" + ValidationConsts.ALLOWED_EMAIL_DOMAIN);
            helper.setTo(toEmail);
            helper.setSubject(STAFF_CREATION_EMAIL_SUBJECT);

            String emailBody = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                            .footer { margin-top: 30px; font-size: 0.9em; color: #666; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h2>Hola,</h2>
                            <p>Se ha creado una cuenta de personal para ti en el Sistema de Comedor Universitario.</p>
                            <p>Tu contraseña  es: <strong>%s</strong></p>
                            <div class="footer">
                                <p>Saludos,<br>Equipo del Sistema de Comedor Universitario</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """, temporaryPassword);

            helper.setText(emailBody, true); // El 'true' indica que es HTML

            emailSender.send(message);

           log.debug("=== STAFF EMAIL SENT ===");
           log.debug("To: " + toEmail);
           log.debug("========================");

        } catch (MessagingException e) {
           log.error("=== STAFF EMAIL ERROR ===");
           log.error("Error sending email to " + toEmail);
           log.error("Error type: " + e.getClass().getSimpleName());
           log.error("Error message: " + e.getMessage());
            if (e.getCause() != null) {
               log.error("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
           log.error("===========================");
            throw new FailedToSendEmailException(STAFF_CREATION_EMAIL_SUBJECT, e);

        }
    }

}