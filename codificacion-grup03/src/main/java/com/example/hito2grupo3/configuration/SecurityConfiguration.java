package com.example.hito2grupo3.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    
                    auth.requestMatchers("/", "/login", "/registro", "/error").permitAll();
                    
                    auth.requestMatchers("/cierreCaja" ,"/empleadoDashboard","/recaudacion","/reciboPdf","/recibo-pdf").permitAll();
                    auth.requestMatchers("/css/**", "/images/**", "/js/**", "/vendor/bootstrap/css/**",
                            "/vendor/jquery/**", "/vendor/bootstrap/js/**", "/api/v1/**").permitAll();
                    
                    auth.requestMatchers("/admin/**").hasRole("ADMIN");
                    auth.requestMatchers("/liquidacion/**").hasAnyRole("ADMIN", "RESPONSABLE");
                    auth.anyRequest().authenticated();
                })
                .formLogin(login -> {
                    login.loginPage("/login"); // Tu ruta de login
                    login.loginProcessingUrl("/loginprocess");
                    
                    
                    login.usernameParameter("email"); 
                    login.passwordParameter("password");
                    
                    // Redirección (Admin vs Usuario)
                    login.successHandler((request, response, authentication) -> {
                        boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                        
                        if (isAdmin) {
                            response.sendRedirect("/admin/dashboard"); 
                        } else {
                            response.sendRedirect("/"); 
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
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(
            com.example.hito2grupo3.repositories.UsuarioRepository usuarioRepo) {
        return email -> usuarioRepo.findByEmail(email)
            .map(u -> org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRol().getNombre().replace("ROLE_", "")) 
                .build())
            .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Email no encontrado"));
    }
}