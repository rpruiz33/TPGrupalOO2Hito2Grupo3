package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

/** DTO de reporte, no persistente. */
public record LiquidacionDTO(
        Long empleadoId,
        String nombreCompleto,
        String email,
        String dni,
        String rol,
        long aniosAntiguedad,
        BigDecimal sueldoBase,
        BigDecimal adicional,   
        int periodos,           
        BigDecimal total) {

    public LiquidacionDTO(
            Long empleadoId,
            String nombreCompleto,
            String dni,
            String rol,
            long aniosAntiguedad,
            BigDecimal sueldoBase,
            BigDecimal adicional,
            int periodos,
            BigDecimal total) {
        this(empleadoId, nombreCompleto, "", dni, rol, aniosAntiguedad, sueldoBase, adicional, periodos, total);
    }
}
