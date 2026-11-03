package com.example.hito2grupo3.configuration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.hito2grupo3.entities.CategoriaCocina;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;

@Configuration
public class DataCarga {

    @Bean
    @Order(1)
    public CommandLineRunner inicializarCategoriasCocina(CategoriaCocinaRepository categoriaRepo) {
        return args -> {
            // Lista oficial de 15 categorías de cocina solicitadas
            Map<String, BigDecimal> categorias15 = new LinkedHashMap<>();
            categorias15.put("Chef Principal", new BigDecimal("150000.00"));
            categorias15.put("Subchef / Segundo de Cocina", new BigDecimal("130000.00"));
            categorias15.put("Parrillero Especialista", new BigDecimal("120000.00"));
            categorias15.put("Asador Criollo", new BigDecimal("115000.00"));
            categorias15.put("Cocinero Principal", new BigDecimal("110000.00"));
            categorias15.put("Maestro Pizzero", new BigDecimal("100000.00"));
            categorias15.put("Minutero", new BigDecimal("95000.00"));
            categorias15.put("Pastelero y Repostero", new BigDecimal("90000.00"));
            categorias15.put("Cocinero de Sándwiches Gourmet", new BigDecimal("85000.00"));
            categorias15.put("Sushiman y Especialidades", new BigDecimal("125000.00"));
            categorias15.put("Cocinero Ayudante", new BigDecimal("80000.00"));
            categorias15.put("Ayudante de Cocina", new BigDecimal("70000.00"));
            categorias15.put("Comis de Cuisine", new BigDecimal("65000.00"));
            categorias15.put("Auxiliar de Despacho y Montaje", new BigDecimal("55000.00"));
            categorias15.put("Bachero / Lavaplatos", new BigDecimal("50000.00"));

            // Asegurar que todas las categorías existentes tengan activo = true si estaban en false por el update de DDL
            List<CategoriaCocina> existentes = categoriaRepo.findAll();
            for (CategoriaCocina c : existentes) {
                if (!c.isActivo()) {
                    c.setActivo(true);
                    categoriaRepo.save(c);
                }
            }

            // Insertar o activar las 15 categorías
            for (Map.Entry<String, BigDecimal> entry : categorias15.entrySet()) {
                String nombre = entry.getKey();
                BigDecimal plus = entry.getValue();

                CategoriaCocina cat = categoriaRepo.findByNombreIgnoreCase(nombre).orElse(null);
                if (cat == null) {
                    cat = new CategoriaCocina(null, nombre, plus, true);
                    categoriaRepo.save(cat);
                } else if (!cat.isActivo()) {
                    cat.setActivo(true);
                    cat.setPlusFijo(plus);
                    categoriaRepo.save(cat);
                }
            }
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner generarHash(PasswordEncoder passwordEncoder) {
        return args -> {
            String password = "Epicentro2026";
            String hash = passwordEncoder.encode(password);

            System.out.println("--------------------------------");
            System.out.println("HASH PARA CargaDatos.sql:");
            System.out.println(hash);
            System.out.println("--------------------------------");
            System.out.println("Verificación: " + passwordEncoder.matches(password, hash));
        };
    }
}

