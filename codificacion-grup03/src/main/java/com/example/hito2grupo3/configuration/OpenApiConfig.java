package com.example.hito2grupo3.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuración central de Swagger / OpenAPI 3 para la API REST del Hito 3.
 * Interfaz gráfica disponible en: /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI epicentroGourmetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Epicentro Gourmet - API REST")
                        .description("Grupo 3 - Hito 3 - Orientación a Objetos II (UNLa). "
                                + "Documentación interactiva de los endpoints REST para gestión de "
                                + "festivales, categorías de cocina, liquidaciones, rentabilidad neta y cierres de caja.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipo Grupo 03 - Epicentro Gourmet")
                                .url("https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3")));
    }
}

