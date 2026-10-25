package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface ILiquidacionService {

    ResultadoLiquidacion liquidar(String emailResponsable, Long festivalId);

    ResultadoLiquidacion liquidarPorUnidad(Long unidadId, Long festivalId);

    ResultadoLiquidacion liquidarTodos(Long festivalId);

    ResultadoLiquidacion liquidarEmpleado(String emailEmpleado, Long festivalId);

    long diasDelFestival(LocalDate inicio, LocalDate fin);

    long aniosAntiguedad(LocalDate fechaIngreso, LocalDate referencia);

    record ResultadoLiquidacion(
            String unidad,
            long diasFestival,
            Set<LiquidacionDTO> liquidaciones,
            BigDecimal totalGeneral) {
    }
}