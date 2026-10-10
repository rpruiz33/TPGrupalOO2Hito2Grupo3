package com.example.hito2grupo3.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.services.RecaudacionService;

@Service
public class RecaudacionServiceImpl implements RecaudacionService {

    private final PedidoRepository pedidoRepository;
    private final FestivalRepository festivalRepository;

    public RecaudacionServiceImpl(PedidoRepository pedidoRepository, FestivalRepository festivalRepository) {
        this.pedidoRepository = pedidoRepository;
        this.festivalRepository = festivalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentaDTO obtenerReporteVenta(Long festivalId) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new IllegalArgumentException("Festival con ID " + festivalId + " no encontrado.");
        }
        return pedidoRepository.obtenerReporteVentaPorFestival(festivalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingUnidadDTO> obtenerRankingUnidades(Long festivalId) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new IllegalArgumentException("Festival con ID " + festivalId + " no encontrado.");
        }
        return pedidoRepository.obtenerRankingPorFestival(festivalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarFestivales() {
        return festivalRepository.findAll();
    }

    
}
