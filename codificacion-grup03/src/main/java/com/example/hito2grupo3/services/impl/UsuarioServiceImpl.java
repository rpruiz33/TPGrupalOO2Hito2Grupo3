package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.events.UsuarioCreadoEvent;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IUsuarioService;
import com.example.hito2grupo3.services.PasswordGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              PasswordGenerator passwordGenerator,
                              ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordGenerator = passwordGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        String passwordPlano = passwordGenerator.generarPassword();

        usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
        usuario.setDebeCambiarPassword(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioCreadoEvent(usuarioGuardado.getEmail(), passwordPlano));

        return usuarioGuardado;
    }

    @Override
    @Transactional
    public void reenviarCredenciales(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        String nuevaPasswordPlana = passwordGenerator.generarPassword();
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPasswordPlana));
        usuario.setDebeCambiarPassword(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioCreadoEvent(usuarioGuardado.getEmail(), nuevaPasswordPlana));
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, String passwordActual, String nuevaPassword, String confirmarPassword) {
        if (nuevaPassword == null || nuevaPassword.trim().length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el email: " + email));

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (passwordEncoder.matches(nuevaPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.setDebeCambiarPassword(false);

        usuarioRepository.save(usuario);
    }
}
