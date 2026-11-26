package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de rentabilidad neta de una unidad de venta en un festival")
public class ReporteRentabilidadNetaDTO {

    @Schema(description = "ID del festival", example = "1")
    private Long festivalId;

    @Schema(description = "Nombre del festival", example = "Epicentro Gourmet UNLa (Edicion Actual)")
    private String nombreFestival;

    @Schema(description = "ID de la unidad de venta", example = "1")
    private Long unidadVentaId;

    @Schema(description = "Nombre de la unidad de venta", example = "La Burguesa")
    private String nombreUnidad;

    @Schema(description = "Código comercial de la unidad", example = "FT-0000001")
    private String codigoUnidad;

    @Schema(description = "Tipo de unidad de venta", example = "FoodTruck")
    private String tipoUnidad;

    @Schema(description = "Total de ingresos por pedidos", example = "450000.00")
    private BigDecimal totalIngresos;      // Pedidos totales

    @Schema(description = "Costo total de producción de platos", example = "135000.00")
    private BigDecimal costosPlatos;       // Costo de producción de platos

    @Schema(description = "Sueldos liquidados del personal", example = "120000.00")
    private BigDecimal sueldos;            // Sueldos liquidados del personal

    @Schema(description = "Canon proporcional de la unidad", example = "45000.00")
    private BigDecimal canon;              // Canon de la unidad

    @Schema(description = "Suma de costos (platos + sueldos + canon)", example = "300000.00")
    private BigDecimal totalCostos;        // costosPlatos + sueldos + canon

    @Schema(description = "Rentabilidad neta resultante", example = "150000.00")
    private BigDecimal rentabilidadNeta;   // (totalIngresos - costosPlatos) - sueldos - canon

    @Schema(description = "Cantidad de pedidos incluidos", example = "42")
    private Long cantidadPedidos;

    @Schema(description = "Período analizado", example = "2026-11-15 al 2026-11-30")
    private String periodo;

    // Constructor simplificado para compatibilidad si hiciera falta
    public ReporteRentabilidadNetaDTO(Long festivalId, String nombreFestival,
                                     BigDecimal totalIngresos, BigDecimal totalCostos,
                                     BigDecimal rentabilidadNeta, Long cantidadPedidos) {
        this.festivalId = festivalId;
        this.nombreFestival = nombreFestival;
        this.totalIngresos = totalIngresos;
        this.totalCostos = totalCostos;
        this.rentabilidadNeta = rentabilidadNeta;
        this.cantidadPedidos = cantidadPedidos;
    }
}