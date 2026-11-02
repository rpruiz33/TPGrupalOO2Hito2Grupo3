package com.example.hito2grupo3.configuration;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores para el cálculo del canon de unidades de venta.
 * Evita valores hardcodeados en el código de negocio (nota 4 del enunciado del TP).
 * Se configuran en application.yml bajo el prefijo "canon".
 * Habilitado con @ConfigurationPropertiesScan en la clase main.
 */
@ConfigurationProperties(prefix = "canon")
public record CanonProperties(
        BigDecimal costoPorM2,
        BigDecimal plusElectricidadFoodTruck,
        BigDecimal costoPorMinutoMontajePuesto) {

    public BigDecimal costoPorM2OrDefault() {
        return costoPorM2 != null ? costoPorM2 : new BigDecimal("500");
    }

    public BigDecimal plusElectricidadOrDefault() {
        return plusElectricidadFoodTruck != null ? plusElectricidadFoodTruck : new BigDecimal("2000");
    }

    public BigDecimal costoPorMinutoMontajeOrDefault() {
        return costoPorMinutoMontajePuesto != null ? costoPorMinutoMontajePuesto : new BigDecimal("10");
    }
}

