package com.example.hito2grupo3.services.api.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.api.RentabilidadRequestDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import com.example.hito2grupo3.services.api.IRentabilidadApiService;

/**
 * Implementación del servicio REST para cálculo y consulta de rentabilidad neta.
 * Reutiliza {@link ReporteRentabilidadNetaDTO}.
 */
@Service
public class RentabilidadApiServiceImpl implements IRentabilidadApiService {

    private final IRentabilidadNetaService rentabilidadNetaService;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public RentabilidadApiServiceImpl(
            IRentabilidadNetaService rentabilidadNetaService,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.rentabilidadNetaService = rentabilidadNetaService;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteRentabilidadNetaDTO calcularRentabilidadUnidad(RentabilidadRequestDTO request) {
        Festival festival = buscarFestivalOExcepcion(request.getFestivalId());
        if (!unidadVentaRepository.existsById(request.getUnidadVentaId())) {
            throw new NoSuchElementException(
                    "No existe la unidad de venta con id " + request.getUnidadVentaId() + ".");
        }

        LocalDate desde = request.getFechaDesde() != null ? request.getFechaDesde() : festival.getFechaInicio();
        LocalDate hasta = request.getFechaHasta() != null ? request.getFechaHasta() : festival.getFechaFin();

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        return rentabilidadNetaService.calcularRentabilidadUnidadEntreFechas(
                festival.getId(), request.getUnidadVentaId(), desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteRentabilidadNetaDTO> buscarRentabilidadFiltrada(
            Long festivalId,
            Long unidadVentaId,
            String tipoUnidad,
            String temporada,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal rentabilidadMinima) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (festivalId != null && !festivalRepository.existsById(festivalId)) {
            throw new NoSuchElementException("No existe el festival con id " + festivalId + ".");
        }
        if (unidadVentaId != null && !unidadVentaRepository.existsById(unidadVentaId)) {
            throw new NoSuchElementException("No existe la unidad de venta con id " + unidadVentaId + ".");
        }

        String tipoNorm = normalizar(tipoUnidad);
        String temporadaNorm = normalizar(temporada);

        List<Festival> festivales = festivalId != null
                ? List.of(buscarFestivalOExcepcion(festivalId))
                : rentabilidadNetaService.listarFestivales();

        List<ReporteRentabilidadNetaDTO> resultados = new ArrayList<>();
        for (Festival fest : festivales) {
            if (temporadaNorm != null && (fest.getTemporada() == null
                    || !fest.getTemporada().toLowerCase(Locale.ROOT).equals(temporadaNorm))) {
                continue;
            }
            LocalDate desde = fechaDesde != null ? fechaDesde : fest.getFechaInicio();
            LocalDate hasta = fechaHasta != null ? fechaHasta : fest.getFechaFin();
            if (desde != null && hasta != null && desde.isAfter(hasta)) {
                continue;
            }

            List<UnidadVenta> unidades = rentabilidadNetaService.listarUnidadesPorFestival(fest.getId());
            for (UnidadVenta uv : unidades) {
                if (unidadVentaId != null && !unidadVentaId.equals(uv.getId())) {
                    continue;
                }
                ReporteRentabilidadNetaDTO dto = rentabilidadNetaService.calcularRentabilidadUnidadEntreFechas(
                        fest.getId(), uv.getId(), desde, hasta);

                if (tipoNorm != null && (dto.getTipoUnidad() == null
                        || !dto.getTipoUnidad().toLowerCase(Locale.ROOT).equals(tipoNorm))) {
                    continue;
                }
                if (rentabilidadMinima != null && dto.getRentabilidadNeta() != null
                        && dto.getRentabilidadNeta().compareTo(rentabilidadMinima) < 0) {
                    continue;
                }
                resultados.add(dto);
            }
        }
        return resultados;
    }

    private Festival buscarFestivalOExcepcion(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el festival con id " + id + "."));
    }

    private String normalizar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase(Locale.ROOT);
    }
}
