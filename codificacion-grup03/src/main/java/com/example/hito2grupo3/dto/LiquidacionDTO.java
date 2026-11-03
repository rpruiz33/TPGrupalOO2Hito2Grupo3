package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

/**
 * DTO de liquidación de haberes (no persistente).
 *
 * Fórmula:
 *   total = sueldoBase + montoAntiguedad + plusCategoria
 *
 * Donde:
 *   montoAntiguedad = aniosAntiguedad × $5.000
 *   plusCategoria   = $150.000 (Cocinero) | $100.000 (Ayudante) | $50.000 (Lavaplatos) | $0 (Cajero)
 */
public record LiquidacionDTO(
        Long empleadoId,
        String nombreCompleto,
        String email,
        String dni,
        String rol,
        long aniosAntiguedad,
        BigDecimal sueldoBase,
        BigDecimal montoAntiguedad,   // aniosAntiguedad × $5.000
        BigDecimal plusCategoria,      // plus según rol en cocina (0 para Cajero)
        BigDecimal total) {

    /** Alias de compatibilidad: devuelve plusCategoria (para cajero devuelve montoAntiguedad). */
    public BigDecimal adicional() {
        return plusCategoria != null ? plusCategoria : montoAntiguedad;
    }

    /** Alias de compatibilidad (ya no se usa factor de períodos). */
    public int periodos() {
        return 1;
    }
}
