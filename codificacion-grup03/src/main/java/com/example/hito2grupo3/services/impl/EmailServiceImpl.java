package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.services.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender,
                        @Value("${app.mail.from:no-reply@epicentro.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void enviarCredenciales(String destinatario, String passwordPlano) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromEmail);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Bienvenido a Epicentro - Credenciales de acceso");
            mensaje.setText(construirCuerpo(destinatario, passwordPlano));

            mailSender.send(mensaje);
            log.info("Correo con credenciales enviado exitosamente al destinatario: {}", destinatario);
        } catch (Exception e) {
            // Seguridad: nunca loguear la contraseña ni en INFO, ni en DEBUG ni en excepciones
            log.error("Fallo el envio de credenciales por email a {}: {}", destinatario, e.getMessage());
            // TODO: Punto de reintento o encolado (ej. persistir tarea en tabla de reintentos o dead-letter queue)
        }
    }

    @Override
    public String construirCuerpo(String email, String passwordPlano) {
        return """
                Estimado/a usuario/a,

                Se ha creado exitosamente su cuenta en el sistema de Epicentro.
                A continuacion, se detallan sus datos de acceso:

                Usuario / Email: %s
                Contraseña temporal: %s

                Por motivos de seguridad, es obligatorio modificar esta contraseña temporal en su primer ingreso al sistema.

                Atentamente,
                Equipo de Administracion - Epicentro
                """.formatted(email, passwordPlano);
    }
}
