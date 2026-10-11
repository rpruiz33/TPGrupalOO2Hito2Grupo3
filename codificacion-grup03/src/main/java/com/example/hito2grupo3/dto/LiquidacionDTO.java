package com.example.hito2grupo3.dto;

import java.math.BigDecimal;

/** DTO de reporte, no persistente. */
public record LiquidacionDTO(
        Long empleadoId,
        String nombreCompleto,
        String dni,
        String rol,
        long aniosAntiguedad,
        BigDecimal sueldoBase,
        BigDecimal adicional,   // plus por categoría (cocina) o antigüedad total (cajero)
        int periodos,           // cantidad de tramos de 15 días
        BigDecimal total) {
}
