package com.example.hito2grupo3.configuration;

import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.UsuarioRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final UsuarioRepository usuarioRepo;

    public SecurityConfiguration(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> {

                    // Rutas públicas
                    auth.requestMatchers(
                            "/", "/login", "/registro", "/error"
                    ).permitAll();

                    // Recursos estáticos
                    auth.requestMatchers(
                            "/css/**",
                            "/images/**",
                            "/js/**",
                            "/vendor/bootstrap/css/**",
                            "/vendor/jquery/**",
                            "/vendor/bootstrap/js/**",
                            "/api/v1/**"
                    ).permitAll();
                    auth.requestMatchers("/admin/**").hasRole("ADMIN");
                    auth.requestMatchers("/liquidacion", "/liquidacion/**", "/reciboPdf/**", "/recibo-pdf/**")
                            .hasAnyRole("ADMIN", "RESPONSABLE", "EMPLEADO", "USER");
                    auth.requestMatchers("/cajero/**")
                            .hasAnyRole("ADMIN", "EMPLEADO", "USER");
                    auth.anyRequest().authenticated();
                })

                .formLogin(login -> {

                    login.loginPage("/login");
                    login.loginProcessingUrl("/loginprocess");
                    login.usernameParameter("email");
                    login.passwordParameter("password");

                    login.successHandler((request, response, authentication) -> {

                        String email = authentication.getName();

                        Usuario usuario = usuarioRepo.findByEmail(email)
                                .orElse(null);

                        // Forzar el cambio de contraseña si corresponde
                        if (usuario != null && usuario.isDebeCambiarPassword()) {
                            response.sendRedirect("/cambiar-password");
                            return;
                        }

                        // Obtener el rol del usuario autenticado
                        String rol = authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .filter(authority -> authority.startsWith("ROLE_"))
                                .findFirst()
                                .orElse("");

                        // Redirigir según el rol
                        switch (rol) {

                            case "ROLE_ADMIN":
                                response.sendRedirect("/admin/dashboard");
                                break;

                            case "ROLE_RESPONSABLE":
                                response.sendRedirect("/empleado/index");
                                break;

                            case "ROLE_EMPLEADO":
                                response.sendRedirect("/empleado/index");
                                break;

                            case "ROLE_CAJERO":
                                response.sendRedirect("/cajero/index");
                                break;

                            case "ROLE_COCINERO":
                                response.sendRedirect("/empleado/index");
                                break;

                            default:
                                response.sendRedirect("/login?error=rol");
                                break;
                        }
                    });

                    login.failureUrl("/login?error=true");
                    login.permitAll();
                })

                .logout(logout -> {
                    logout.logoutUrl("/logout");
                    logout.logoutSuccessUrl("/login?logout=true");
                    logout.permitAll();
                })

                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return email -> usuarioRepo.findByEmail(email)
                .map(u -> org.springframework.security.core.userdetails.User.builder()
                        .username(u.getEmail())
                        .password(u.getPasswordHash())
                        .roles(u.getRol().getNombre().replace("ROLE_", ""))
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Email no encontrado"));
    }
}
