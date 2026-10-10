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
            
            // 1. Creamos el ROL_ADMIN si no existe en la base de datos
            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseGet(() -> {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre("ROLE_ADMIN");
                return rolRepository.save(nuevoRol);
            });

            // 2. Creamos el ROL_USER si no existe (necesario para los registros normales)
            Rol rolUser = rolRepository.findByNombre("ROLE_USER").orElseGet(() -> {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre("ROLE_USER");
                return rolRepository.save(nuevoRol);
            });

            // 3. Creamos el usuario Administrador por defecto si no existe
            String emailAdmin = "admin@epicentro.com";
            if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setEmail(emailAdmin);
                admin.setPasswordHash(passwordEncoder.encode("admin123")); // Clave por defecto
                admin.setRol(rolAdmin);
                
                usuarioRepository.save(admin);
                System.out.println("Usuario administrador creado con éxito (admin@epicentro.com / admin123)");
            }
        };
    }
}