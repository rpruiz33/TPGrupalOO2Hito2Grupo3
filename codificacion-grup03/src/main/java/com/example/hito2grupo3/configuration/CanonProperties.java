package com.example.hito2grupo3.configuration;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

