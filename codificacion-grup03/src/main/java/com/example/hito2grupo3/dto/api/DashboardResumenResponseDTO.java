package com.example.hito2grupo3.dto.api;

import java.math.BigDecimal;
import java.util.List;

import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO consolidado del dashboard financiero administrativo para un festival o temporada.
 */
@Schema(description = "Resumen ejecutivo financiero de festivales y unidades de venta")
public record DashboardResumenResponseDTO(
        @Schema(description = "ID del festival seleccionado", example = "1")
        Long festivalId,

        @Schema(description = "Nombre del festival seleccionado", example = "Epicentro Gourmet Primavera")
        String nombreFestival,

        @Schema(description = "Temporada del festival seleccionado", example = "Primavera")
        String temporada,

        @Schema(description = "Total de ingresos acumulados", example = "950000.00")
        BigDecimal totalIngresos,

        @Schema(description = "Total de costos acumulados", example = "420000.00")
        BigDecimal totalCostos,

        @Schema(description = "Rentabilidad neta total del festival", example = "530000.00")
        BigDecimal rentabilidadNetaTotal,

        @Schema(description = "Cantidad de unidades analizadas", example = "4")
        int cantidadUnidades,

        @Schema(description = "Detalle individual por unidad de venta")
        List<ReporteRentabilidadNetaDTO> detallePorUnidad) {
}
