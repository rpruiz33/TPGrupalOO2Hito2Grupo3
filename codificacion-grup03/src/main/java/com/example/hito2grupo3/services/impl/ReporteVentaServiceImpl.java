package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.services.IReporteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteVentaServiceImpl implements IReporteVentaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public ReporteVentaDTO obtenerTotalesFestival(Long festivalId) {
        ReporteVentaDTO reporte = pedidoRepository.obtenerReporteVentaPorFestival(festivalId);
        if (reporte == null || reporte.getFestivalId() == null) {
            throw new RuntimeException("Festival no encontrado o sin datos para el ID: " + festivalId);
        }
        return reporte;
    }

    @Override
    public List<RankingUnidadDTO> obtenerRankingFestival(Long festivalId) {
        return pedidoRepository.obtenerRankingPorFestival(festivalId);
    }
}