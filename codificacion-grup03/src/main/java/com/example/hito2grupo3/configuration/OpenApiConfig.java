package com.example.hito2grupo3.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuración de Swagger / OpenAPI.
 * UI disponible en: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI epicentroGourmetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Epicentro Gourmet - API REST")
                        .description("Grupo 3 - Hito 3 - Orientación a Objetos II. "
                                + "Endpoints REST del sistema de gestión de festivales.")
                        .version("v1"));
    }
}
