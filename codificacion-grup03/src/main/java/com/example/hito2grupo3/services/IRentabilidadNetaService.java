package com.example.hito2grupo3.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;

public interface IRentabilidadNetaService {

    ReporteVentaDTO obtenerReporteVenta(Long festivalId);

    List<RankingUnidadDTO> obtenerRankingUnidades(Long festivalId);

    List<Festival> listarFestivales();

    List<UnidadVenta> listarUnidadesPorFestival(Long festivalId);

    ReporteVentaDTO obtenerReporteVentaEntreFechas(Long festivalId);

    Object obtenerReporteVentaEntreFechas(Long festivalId, LocalDate fechaDesde, LocalDate fechaHasta);

    Map<String, Object> obtenerReporteRentabilidad(Long festivalId, Long unidadVentaId, LocalDate fechaDesde, LocalDate fechaHasta);

    ReporteRentabilidadNetaDTO calcularRentabilidadUnidad(Long festivalId, Long unidadVentaId);

    ReporteRentabilidadNetaDTO calcularRentabilidadUnidadEntreFechas(Long festivalId, Long unidadVentaId, LocalDate fechaDesde, LocalDate fechaHasta);
}
