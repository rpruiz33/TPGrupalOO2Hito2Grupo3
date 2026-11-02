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

    com.example.hito2grupo3.entities.Festival resolverFestivalPorDefecto(Long festivalId);

    java.util.List<com.example.hito2grupo3.entities.Festival> listarFestivales();

    java.util.List<com.example.hito2grupo3.entities.UnidadVenta> listarTodasLasUnidades();

    ResultadoLiquidacion liquidarSegunRol(String email, Long festivalId, Long unidadId, String codigo, boolean esAdmin, boolean esResponsable);

    java.util.Map<String, Object> obtenerModeloRecibo(Long id, Long festivalId, Long unidadId, String email, boolean esAdmin, boolean esEmpleado);

    record ResultadoLiquidacion(
            String unidad,
            long diasFestival,
            Set<LiquidacionDTO> liquidaciones,
            BigDecimal totalGeneral) {
    }
}