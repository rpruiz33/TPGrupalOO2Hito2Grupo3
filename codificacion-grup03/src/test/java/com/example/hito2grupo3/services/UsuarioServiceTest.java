package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.events.UsuarioCreadoEvent;
import com.example.hito2grupo3.events.UsuarioCreadoListener;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.impl.EmailServiceImpl;
import com.example.hito2grupo3.services.impl.PasswordGeneratorImpl;
import com.example.hito2grupo3.services.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGeneratorImpl passwordGenerator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailServiceImpl emailService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioCreadoListener usuarioCreadoListener;

    @BeforeEach
    void setUp() {
        usuarioCreadoListener = new UsuarioCreadoListener(emailService);

        // Al publicar el evento, simulamos la delegación al listener que interactúa con EmailService
        lenient().doAnswer(invocation -> {
            UsuarioCreadoEvent event = invocation.getArgument(0);
            usuarioCreadoListener.handleUsuarioCreado(event);
            return null;
        }).when(eventPublisher).publishEvent(any(UsuarioCreadoEvent.class));
    }

    @Test
    void crearUsuario_GeneraPasswordHasheaYInvocaEnvioEmailUnaVez() {
        // Arrange
        String email = "juan.perez@epicentro.com";
        String plainPasswordGenerado = "Secur3P@sswrd";
        String hashEsperado = "$2a$10$abcdefghijklmnopqrstuvwxyz123456";

        Usuario usuarioEntrada = new Usuario();
        usuarioEntrada.setEmail(email);
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_EMPLEADO");
        usuarioEntrada.setRol(rol);

        when(passwordGenerator.generarPassword()).thenReturn(plainPasswordGenerado);
        when(passwordEncoder.encode(plainPasswordGenerado)).thenReturn(hashEsperado);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario usuarioCreado = usuarioService.crearUsuario(usuarioEntrada);

        // Assert
        assertNotNull(usuarioCreado);
        assertEquals(email, usuarioCreado.getEmail());
        assertEquals(hashEsperado, usuarioCreado.getPasswordHash(), "Debe guardarse el hash, nunca el texto plano");
        assertTrue(usuarioCreado.isDebeCambiarPassword(), "Debe requerir cambio obligatorio de contraseña");

        // 1. Se hashea la contraseña generada
        verify(passwordGenerator, times(1)).generarPassword();
        verify(passwordEncoder, times(1)).encode(plainPasswordGenerado);

        // 2. Se persiste en el repositorio con el hash
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(usuarioCaptor.capture());
        assertEquals(hashEsperado, usuarioCaptor.getValue().getPasswordHash());
        assertNotEquals(plainPasswordGenerado, usuarioCaptor.getValue().getPasswordHash());

        // 3. Se dispara el evento y se invoca el envío de credenciales con EmailService una sola vez
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioCreadoEvent.class));
        verify(emailService, times(1)).enviarCredenciales(email, plainPasswordGenerado);
    }

    @Test
    void cambiarPassword_Exitoso() {
        String email = "juan.perez@epicentro.com";
        String passActual = "TempPass123!";
        String passNueva = "MiNuevaClaveSegura2026!";

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash("hash_actual");
        usuario.setDebeCambiarPassword(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches(passActual, "hash_actual")).thenReturn(true);
        when(passwordEncoder.matches(passNueva, "hash_actual")).thenReturn(false);
        when(passwordEncoder.encode(passNueva)).thenReturn("hash_nuevo");

        usuarioService.cambiarPassword(email, passActual, passNueva, passNueva);

        assertEquals("hash_nuevo", usuario.getPasswordHash());
        assertFalse(usuario.isDebeCambiarPassword(), "debeCambiarPassword debe ser false luego del cambio");
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void cambiarPassword_PasswordActualIncorrecta_LanzaExcepcion() {
        String email = "juan.perez@epicentro.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash("hash_actual");

        when(usuarioRepository.findByEmail(email)).thenReturn(java.util.Optional.of(usuario));
        when(passwordEncoder.matches("clave_erronea", "hash_actual")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.cambiarPassword(email, "clave_erronea", "NuevaClave123!", "NuevaClave123!")
        );
    }
}
