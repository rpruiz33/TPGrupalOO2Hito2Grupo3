package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRentabilidadNetaDTO {

    private Long festivalId;
    private String nombreFestival;
    private Long unidadVentaId;
    private String nombreUnidad;
    private String codigoUnidad;
    private String tipoUnidad;
    private BigDecimal totalIngresos;      // Pedidos totales
    private BigDecimal costosPlatos;       // Costo de producción de platos
    private BigDecimal sueldos;            // Sueldos liquidados del personal
    private BigDecimal canon;              // Canon de la unidad
    private BigDecimal totalCostos;        // costosPlatos + sueldos + canon
    private BigDecimal rentabilidadNeta;   // (totalIngresos - costosPlatos) - sueldos - canon
    private Long cantidadPedidos;
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