package com.example.hito2grupo3.services;

import java.util.List;

import com.example.hito2grupo3.dto.RankingUnidadVentaDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;

public interface RecaudacionService {

    ReporteVentaDTO obtenerReporteVenta(Long festivalId);

    List<RankingUnidadVentaDTO> obtenerRankingUnidades(Long festivalId);

    List<Festival> listarFestivales();
}
