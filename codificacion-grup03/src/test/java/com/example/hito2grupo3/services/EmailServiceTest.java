package com.example.hito2grupo3.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.hito2grupo3.services.impl.EmailServiceImpl;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, "no-reply@epicentro.com");
    }

    @Test
    void enviarCredenciales_EnviaMensajeCorrecto() {
        String destinatario = "test@epicentro.com";
        String password = "TempPassw0rd!";

        emailService.enviarCredenciales(destinatario, password);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage mensaje = captor.getValue();
        assertEquals("no-reply@epicentro.com", mensaje.getFrom());
        assertEquals(destinatario, Objects.requireNonNull(mensaje.getTo())[0]);
        assertTrue(Objects.requireNonNull(mensaje.getSubject()).contains("Credenciales de acceso"));
        assertTrue(Objects.requireNonNull(mensaje.getText()).contains(destinatario));
        assertTrue(mensaje.getText().contains(password));
    }

    @Test
    void enviarCredenciales_CuandoFallaSmtp_NoLanzaExcepcion() {
        doThrow(new MailSendException("Conexión SMTP fallida"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // No debe lanzar excepción ni interrumpir el flujo
        assertDoesNotThrow(() -> emailService.enviarCredenciales("error@epicentro.com", "TempPassw0rd!"));
    }
}
