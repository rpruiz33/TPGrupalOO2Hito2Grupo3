
package com.example.hito2grupo3.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataCarga {

    @Bean
    public CommandLineRunner generarHash(
            PasswordEncoder passwordEncoder) {

        return args -> {
            String password = "Epicentro2026";
            String hash = passwordEncoder.encode(password);

            System.out.println("--------------------------------");
            System.out.println("HASH PARA CargaDatos.sql:");
            System.out.println(hash);
            System.out.println("--------------------------------");

            System.out.println(
                "Verificación: " +
                passwordEncoder.matches(password, hash)
            );
        };
    }
}
