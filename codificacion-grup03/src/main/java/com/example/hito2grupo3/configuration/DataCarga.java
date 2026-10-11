package com.example.hito2grupo3.configuration;

import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.RolRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataCarga {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, 
                                          RolRepository rolRepository, 
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            
            // CREACION DE USUARIO  PARA TEST FEDE
            Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre("ADMIN");
                return rolRepository.save(nuevoRol);
            });

            
            Rol rolUser = rolRepository.findByNombre("USER").orElseGet(() -> {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre("USER");
                return rolRepository.save(nuevoRol);
            });

            
            String emailAdmin = "admin@epicentro.com";
            if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setEmail(emailAdmin);
                admin.setPasswordHash(passwordEncoder.encode("admin123")); 
                admin.setRol(rolAdmin);
                
                usuarioRepository.save(admin);
                System.out.println("Usuario administrador creado con éxito (admin@epicentro.com / admin123)");
            }
        };
    }
}