package com.example.hito2grupo3.configuration;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores de liquidación: nada hardcodeado en los métodos (nota 4 del TP).
 * Se configuran en application.properties con el prefijo "sueldos".
 * Habilitar con @ConfigurationPropertiesScan en la clase main.
 */
@ConfigurationProperties(prefix = "sueldos")
public record SueldoProperties(
        BigDecimal base,
        BigDecimal porAnioAntiguedad,
        BigDecimal plusCocinero,
        BigDecimal plusCocineroAyudante,
        BigDecimal plusLavaplatos,
        int diasPorPeriodo,
        BigDecimal porcentajePorPeriodo) {
}
