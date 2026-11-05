package com.example.hito2grupo3.services;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;

import java.util.List;

public interface IReporteVentaService {

    ReporteVentaDTO obtenerTotalesFestival(Long festivalId);

    ReporteVentaDTO obtenerTotalesFestivalYUnidad(Long festivalId, Long unidadVentaId);

    List<RankingUnidadDTO> obtenerRankingFestival(Long festivalId);

    List<RankingUnidadDTO> obtenerRankingFestivalYUnidad(Long festivalId, Long unidadVentaId);
}