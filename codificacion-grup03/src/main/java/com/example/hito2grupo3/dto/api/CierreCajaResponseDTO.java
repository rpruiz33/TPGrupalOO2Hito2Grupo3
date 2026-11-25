package com.example.hito2grupo3.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.hito2grupo3.entities.CierreCaja;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para un registro de cierre de caja.
 */
@Schema(description = "Representación de un cierre o reapertura de caja")
public record CierreCajaResponseDTO(
        @Schema(description = "ID del registro de cierre de caja", example = "1")
        Long id,

        @Schema(description = "Fecha del cierre", example = "25-11-2026")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate fecha,

        @Schema(description = "Monto recaudado", example = "125500.00")
        BigDecimal montoRecaudado,

        @Schema(description = "Estado actual de la caja", example = "CERRADA", allowableValues = { "CERRADA", "REABIERTA" })
        String estado,

        @Schema(description = "ID del cajero", example = "4")
        Long cajeroId,

        @Schema(description = "Nombre completo del cajero", example = "Alejandro Ramirez")
        String cajeroNombre,

        @Schema(description = "ID de la unidad de venta", example = "1")
        Long unidadVentaId,

        @Schema(description = "Nombre comercial de la unidad", example = "Sabores sobre Ruedas")
        String unidadVentaNombre,

        @Schema(description = "Fecha y hora del cierre", example = "25-11-2026 22:15")
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
        LocalDateTime fechaHoraCierre,

        @Schema(description = "Fecha y hora de reapertura (si fue reabierta)", example = "25-11-2026 22:30")
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
        LocalDateTime fechaHoraReapertura,

        @Schema(description = "Motivo de reapertura (si aplica)")
        String motivoReapertura) {

    public static CierreCajaResponseDTO desde(CierreCaja cierre) {
        Long cajId = cierre.getCajero() != null ? cierre.getCajero().getId() : null;
        String cajNombre = cierre.getCajero() != null
                ? cierre.getCajero().getNombre() + " " + cierre.getCajero().getApellido()
                : null;
        Long uvId = cierre.getUnidadVenta() != null ? cierre.getUnidadVenta().getId() : null;
        String uvNombre = cierre.getUnidadVenta() != null ? cierre.getUnidadVenta().getNombreComercial() : null;

        return new CierreCajaResponseDTO(
                cierre.getId(),
                cierre.getFecha(),
                cierre.getMontoRecaudado(),
                cierre.getEstado(),
                cajId,
                cajNombre,
                uvId,
                uvNombre,
                cierre.getFechaHoraCierre(),
                cierre.getFechaHoraReapertura(),
                cierre.getMotivoReapertura());
    }
}

