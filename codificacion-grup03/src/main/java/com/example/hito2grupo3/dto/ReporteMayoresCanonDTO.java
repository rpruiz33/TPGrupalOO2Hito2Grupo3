
package com.example.hito2grupo3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMayoresCanonDTO {

    private String nombreComercial;
    private String codigo;
    private String tipoUnidad;
    private BigDecimal canon;
}

