package com.example.hito2grupo3.configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;

/**
 * Configuración central de Swagger / OpenAPI 3 para la API REST del Hito 3.
 * Interfaz gráfica disponible en: /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Bean
    public OpenAPI epicentroGourmetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Epicentro Gourmet - API REST")
                        .description("Grupo 3 - Hito 3 - Orientación a Objetos II. "
                                + "Endpoints REST del sistema de gestión de festivales.")
                        .version("v1"));
    }

    /**
     * Personaliza el ejemplo del body de POST /api/v1/rentabilidad/calcular
     * usando fechas relativas al día de ejecución (siempre dentro del festival 1).
     * El festival 1 va de CURDATE()-10 a CURDATE()+15, así que tomamos ±5 días.
     */
    @Bean
    public OperationCustomizer rentabilidadEjemploCustomizer() {
        return (operation, handlerMethod) -> {
            String nombre = handlerMethod.getMethod().getName();
            if (!"calcularRentabilidad".equals(nombre)) {
                return operation;
            }

            LocalDate hoy = LocalDate.now();
            String desde = hoy.minusDays(5).format(FMT);
            String hasta = hoy.plusDays(5).format(FMT);

            String json = String.format("""
                    {
                      "festivalId": 1,
                      "unidadVentaId": 1,
                      "fechaDesde": "%s",
                      "fechaHasta": "%s"
                    }""", desde, hasta);

            if (operation.getRequestBody() != null) {
                Content content = operation.getRequestBody().getContent();
                if (content != null) {
                    MediaType mt = content.get("application/json");
                    if (mt == null) {
                        mt = new MediaType();
                        content.addMediaType("application/json", mt);
                    }
                    mt.getExamples().clear();
                    mt.addExamples("Cálculo de rentabilidad", new Example().value(json));
                }
            }
            return operation;
        };
    }
}
