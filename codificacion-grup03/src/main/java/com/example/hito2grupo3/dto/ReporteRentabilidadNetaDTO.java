package com.example.hito2grupo3.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter 
public class ReporteRentabilidadNetaDTO {

  
    private Long festivalId;
    private String nombreFestival;
    private BigDecimal totalIngresos;
    private BigDecimal totalCostos;
    private BigDecimal rentabilidadNeta;
    private Long cantidadPedidos;
}