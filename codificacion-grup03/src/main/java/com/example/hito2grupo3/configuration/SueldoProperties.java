package com.example.hito2grupo3.configuration;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores de liquidación: nada hardcodeado en los métodos (nota 4 del TP).
 * Se configuran en application.yml con el prefijo "sueldos".
 * Habilitado con @ConfigurationPropertiesScan en la clase main.
 */
@ConfigurationProperties(prefix = "sueldos")
public record SueldoProperties(
        BigDecimal base,
        BigDecimal porAnioAntiguedad,
        BigDecimal plusCocinero,
        BigDecimal plusCocineroAyudante,
        BigDecimal plusLavaplatos,
        Integer diasPorPeriodo,
        BigDecimal porcentajePorPeriodo) {

    public BigDecimal baseOrDefault() {
        return base != null ? base : new BigDecimal("100000");
    }

    public BigDecimal porAnioAntiguedadOrDefault() {
        return porAnioAntiguedad != null ? porAnioAntiguedad : new BigDecimal("5000");
    }

    public BigDecimal plusCocineroOrDefault() {
        return plusCocinero != null ? plusCocinero : new BigDecimal("150000");
    }

    public BigDecimal plusCocineroAyudanteOrDefault() {
        return plusCocineroAyudante != null ? plusCocineroAyudante : new BigDecimal("100000");
    }

    public BigDecimal plusLavaplatosOrDefault() {
        return plusLavaplatos != null ? plusLavaplatos : new BigDecimal("50000");
    }

    public int diasPorPeriodoOrDefault() {
        return (diasPorPeriodo != null && diasPorPeriodo > 0) ? diasPorPeriodo : 15;
    }

    public BigDecimal porcentajePorPeriodoOrDefault() {
        return porcentajePorPeriodo != null ? porcentajePorPeriodo : new BigDecimal("0.50");
    }
}
