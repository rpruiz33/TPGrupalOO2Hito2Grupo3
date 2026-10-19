package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteVentaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Método 1: Obtiene solo los totales
    public ReporteVentaDTO obtenerTotalesFestival(Long festivalId) {
        ReporteVentaDTO reporte = pedidoRepository.obtenerReporteVentaPorFestival(festivalId);
        if (reporte == null || reporte.getFestivalId() == null) {
            throw new RuntimeException("Festival no encontrado o sin datos para el ID: " + festivalId);
        }
        return reporte;
    }

    // Método 2: Obtiene la lista del ranking de puestos
    public List<RankingUnidadDTO> obtenerRankingFestival(Long festivalId) {
        return pedidoRepository.obtenerRankingPorFestival(festivalId);
    }
}