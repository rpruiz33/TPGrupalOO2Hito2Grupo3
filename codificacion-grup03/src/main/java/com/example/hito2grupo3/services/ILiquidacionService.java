package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ILiquidacionService {

    ResultadoLiquidacion liquidar(
            String emailResponsable,
            Long festivalId);

    ResultadoLiquidacion liquidarPorUnidad(
            Long unidadId,
            Long festivalId);

    ResultadoLiquidacion liquidarTodos(Long festivalId);

    ResultadoLiquidacion liquidarEmpleado(
            String emailEmpleado,
            Long festivalId);

    long diasDelFestival(LocalDate inicio, LocalDate fin);

    long aniosAntiguedad(
            LocalDate fechaIngreso,
            LocalDate referencia);

    Festival resolverFestivalPorDefecto(Long festivalId);

    List<Festival> listarFestivales();

    List<UnidadVenta> listarTodasLasUnidades();

    ResultadoLiquidacion liquidarSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable);

    ResultadoLiquidacion consultarLiquidacionesRegistradasSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable);

    ResultadoLiquidacion registrarLiquidacionesSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable);

    Map<String, Object> obtenerModeloRecibo(
            Long liquidacionId,
            String email,
            boolean esAdmin,
            boolean esResponsable);

    record ResultadoLiquidacion(
            String unidad,
            long diasFestival,
            Set<LiquidacionDTO> liquidaciones,
            BigDecimal totalGeneral) {
    }
}