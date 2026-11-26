
package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de liquidación de haberes.
 * Representa una liquidación de un empleado para una unidad
 * y un período determinado.
 */
@Schema(description = "Detalle de liquidación de haberes de un empleado")
public record LiquidacionDTO(
        @Schema(description = "ID del empleado", example = "3") Long empleadoId,
        @Schema(description = "Nombre completo del empleado", example = "Juan Pérez") String nombreCompleto,
        @Schema(description = "Email del empleado", example = "cajero@epicentro.com") String email,
        @Schema(description = "DNI del empleado", example = "35123456") String dni,
        @Schema(description = "Rol operativo", example = "Cajero") String rol,
        @Schema(description = "Años de antigüedad", example = "2") long aniosAntiguedad,
        @Schema(description = "Sueldo base proporcional", example = "180000.00") BigDecimal sueldoBase,
        @Schema(description = "Adicional por antigüedad", example = "18000.00") BigDecimal montoAntiguedad,
        @Schema(description = "Plus por categoría", example = "0.00") BigDecimal plusCategoria,
        @Schema(description = "Total neto liquidado", example = "198000.00") BigDecimal total,
        @Schema(description = "ID de la unidad de venta", example = "1") Long unidadId,
        @Schema(description = "Nombre de la unidad de venta", example = "La Burguesa") String unidad,
        @Schema(description = "Inicio del período liquidado", example = "2026-11-15") LocalDate periodoInicio,
        @Schema(description = "Fin del período liquidado", example = "2026-11-30") LocalDate periodoFin,
        @Schema(description = "Días trabajados en el período", example = "15") long diasTrabajados,
        @Schema(description = "Factor de proporcionalidad aplicado", example = "1.00") BigDecimal factorAplicado,
        @Schema(description = "ID de la liquidación persistida", example = "10") Long liquidacionId) {

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
