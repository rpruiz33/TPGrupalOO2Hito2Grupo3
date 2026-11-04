
package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de liquidación de haberes.
 * Representa una liquidación de un empleado para una unidad
 * y un período determinado.
 */
public record LiquidacionDTO(
        Long empleadoId,
        String nombreCompleto,
        String email,
        String dni,
        String rol,
        long aniosAntiguedad,
        BigDecimal sueldoBase,
        BigDecimal montoAntiguedad,
        BigDecimal plusCategoria,
        BigDecimal total,
        Long unidadId,
        String unidad,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        long diasTrabajados,
        BigDecimal factorAplicado,
        Long liquidacionId) {

    /**
     * Constructor compatible con la versión mensual anterior.
     */
    public LiquidacionDTO(
            Long empleadoId,
            String nombreCompleto,
            String email,
            String dni,
            String rol,
            long aniosAntiguedad,
            BigDecimal sueldoBase,
            BigDecimal montoAntiguedad,
            BigDecimal plusCategoria,
            BigDecimal total,
            Long unidadId,
            String unidad,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            long diasTrabajados,
            BigDecimal factorAplicado) {

        this(
                empleadoId,
                nombreCompleto,
                email,
                dni,
                rol,
                aniosAntiguedad,
                sueldoBase,
                montoAntiguedad,
                plusCategoria,
                total,
                unidadId,
                unidad,
                periodoInicio,
                periodoFin,
                diasTrabajados,
                factorAplicado,
                null
        );
    }

    /**
     * Constructor compatible con la versión original.
     */
    public LiquidacionDTO(
            Long empleadoId,
            String nombreCompleto,
            String email,
            String dni,
            String rol,
            long aniosAntiguedad,
            BigDecimal sueldoBase,
            BigDecimal montoAntiguedad,
            BigDecimal plusCategoria,
            BigDecimal total) {

        this(
                empleadoId,
                nombreCompleto,
                email,
                dni,
                rol,
                aniosAntiguedad,
                sueldoBase,
                montoAntiguedad,
                plusCategoria,
                total,
                null,
                null,
                null,
                null,
                0,
                null,
                null
        );
    }

    public BigDecimal adicional() {
        return plusCategoria != null
                ? plusCategoria
                : montoAntiguedad;
    }

    public int periodos() {
        return 1;
    }
}
