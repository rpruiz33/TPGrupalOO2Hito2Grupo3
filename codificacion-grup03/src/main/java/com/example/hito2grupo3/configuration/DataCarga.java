package com.example.hito2grupo3.configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataCarga {

    @Bean
    public CommandLineRunner cargarDatosIniciales(
            DataSource dataSource,
            PasswordEncoder passwordEncoder) {

        return args -> {

            try (Connection connection = dataSource.getConnection()) {

                // Comprobar si la carga inicial ya fue realizada
                boolean existenRoles;

                try (PreparedStatement consulta = connection.prepareStatement(
                        "SELECT COUNT(*) FROM roles");
                     ResultSet resultado = consulta.executeQuery()) {

                    resultado.next();
                    existenRoles = resultado.getInt(1) > 0;
                }

                if (existenRoles) {
                    System.out.println("La base de datos ya tiene roles. Se omite la carga inicial.");
                    return;
                }

                // Generar contraseña cifrada con BCrypt
                String password = "Epicentro2026";
                String hash = passwordEncoder.encode(password);

                // Pasar el hash al script SQL en la misma conexión
                try (PreparedStatement sentencia = connection.prepareStatement(
                        "SET @hash_pw = ?")) {

                    sentencia.setString(1, hash);
                    sentencia.execute();
                }

                System.out.println("--------------------------------");
                System.out.println("Ejecutando carga inicial...");
                System.out.println("Contraseña inicial protegida con BCrypt.");
                System.out.println("--------------------------------");

                // Ejecutar el script utilizando la misma conexión
                ScriptUtils.executeSqlScript(
                        connection,
                        new ClassPathResource("CargaDatos.sql")
                );

                System.out.println("--------------------------------");
                System.out.println("Carga inicial finalizada correctamente.");
                System.out.println("--------------------------------");
            }
        };
    }
}