package com.example.hito2grupo3.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta con el resumen operativo de caja de un cajero.
 */
@Schema(description = "Resumen de ventas y estado de caja de un cajero")
public record ResumenCajaResponseDTO(
        @Schema(description = "Email del cajero", example = "cajero@epicentro.com")
        String emailCajero,

        @Schema(description = "Nombre completo del cajero", example = "Alejandro Ramirez")
        String nombreCajero,

        @Schema(description = "Fecha consultada", example = "25-11-2026")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate fecha,

        @Schema(description = "Recaudación acumulada del cajero", example = "345000.00")
        BigDecimal recaudacionAcumulada,

        @Schema(description = "Cantidad total de cierres registrados", example = "5")
        int cantidadCierresRegistrados) {
}

