package com.example.hito2grupo3.services;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;

public interface IRentabilidadNetaService {

    ReporteVentaDTO obtenerReporteVenta(Long festivalId);

    List<RankingUnidadDTO> obtenerRankingUnidades(Long festivalId);

    List<Festival> listarFestivales();

    ReporteVentaDTO obtenerReporteVentaEntreFechas(Long festivalId);


    Object obtenerReporteVentaEntreFechas(Long festivalId, LocalDate fechaDesde, LocalDate fechaHasta);
}

