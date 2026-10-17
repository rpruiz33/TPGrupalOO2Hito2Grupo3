package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface ILiquidacionService {

    ResultadoLiquidacion liquidar(String emailResponsable, Long festivalId);

    ResultadoLiquidacion liquidarPorUnidad(Long unidadId, Long festivalId);

    long diasDelFestival(LocalDate inicio, LocalDate fin);

    long aniosAntiguedad(LocalDate fechaIngreso, LocalDate referencia);

    // El record se define aquí para que sea el contrato oficial devuelto por el
    // servicio
    record ResultadoLiquidacion(
            String unidad,
            long diasFestival,
            Set<LiquidacionDTO> liquidaciones,
            BigDecimal totalGeneral) {
    }
}