package com.example.hito2grupo3.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.hito2grupo3.services.IEmailService;

@Component
public class UsuarioCreadoListener {

    private static final Logger log = LoggerFactory.getLogger(UsuarioCreadoListener.class);

    private final IEmailService emailService;

    public UsuarioCreadoListener(IEmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleUsuarioCreado(UsuarioCreadoEvent event) {
        log.info("Procesando envio asincrono de credenciales posterior al commit para: {}", event.email());
        emailService.enviarCredenciales(event.email(), event.passwordPlano());
    }
}
