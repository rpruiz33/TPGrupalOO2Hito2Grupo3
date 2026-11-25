package com.example.hito2grupo3.services.api.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.api.DashboardResumenResponseDTO;
import com.example.hito2grupo3.dto.api.RentabilidadRequestDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import com.example.hito2grupo3.services.IReporteVentaService;
import com.example.hito2grupo3.services.api.IRentabilidadApiService;

/**
 * Implementación del servicio REST para cálculo de rentabilidad neta, recaudación y KPIs.
 * Reutiliza {@link ReporteRentabilidadNetaDTO} y {@link RankingUnidadDTO}.
 */
@Service
public class RentabilidadApiServiceImpl implements IRentabilidadApiService {

    private final IRentabilidadNetaService rentabilidadNetaService;
    private final IReporteVentaService reporteVentaService;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public RentabilidadApiServiceImpl(
            IRentabilidadNetaService rentabilidadNetaService,
            IReporteVentaService reporteVentaService,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.rentabilidadNetaService = rentabilidadNetaService;
        this.reporteVentaService = reporteVentaService;
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

    @Override
    @Transactional(readOnly = true)
    public List<RankingUnidadDTO> buscarRecaudacionFiltrada(
            Long festivalId,
            Long unidadVentaId,
            String temporada,
            String tipoUnidad) {

        if (festivalId != null && !festivalRepository.existsById(festivalId)) {
            throw new NoSuchElementException("No existe el festival con id " + festivalId + ".");
        }
        if (unidadVentaId != null && !unidadVentaRepository.existsById(unidadVentaId)) {
            throw new NoSuchElementException("No existe la unidad de venta con id " + unidadVentaId + ".");
        }

        String temporadaNorm = normalizar(temporada);
        String tipoNorm = normalizar(tipoUnidad);

        List<Festival> festivales = festivalId != null
                ? List.of(buscarFestivalOExcepcion(festivalId))
                : rentabilidadNetaService.listarFestivales();

        List<RankingUnidadDTO> rankingTotal = new ArrayList<>();
        for (Festival fest : festivales) {
            if (temporadaNorm != null && (fest.getTemporada() == null
                    || !fest.getTemporada().toLowerCase(Locale.ROOT).equals(temporadaNorm))) {
                continue;
            }
            List<RankingUnidadDTO> rankingFest = unidadVentaId != null
                    ? reporteVentaService.obtenerRankingFestivalYUnidad(fest.getId(), unidadVentaId)
                    : reporteVentaService.obtenerRankingFestival(fest.getId());

            for (RankingUnidadDTO item : rankingFest) {
                if (tipoNorm != null && (item.getTipoUnidad() == null
                        || !item.getTipoUnidad().toLowerCase(Locale.ROOT).equals(tipoNorm))) {
                    continue;
                }
                rankingTotal.add(item);
            }
        }
        return rankingTotal;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponseDTO obtenerDashboardResumen(Long festivalId, String temporada) {
        Festival seleccionado;
        if (festivalId != null) {
            seleccionado = buscarFestivalOExcepcion(festivalId);
        } else {
            String tempNorm = normalizar(temporada);
            seleccionado = rentabilidadNetaService.listarFestivales().stream()
                    .filter(f -> tempNorm == null
                            || (f.getTemporada() != null && f.getTemporada().toLowerCase(Locale.ROOT).equals(tempNorm)))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "No existen festivales registrados para los criterios indicados."));
        }

        List<ReporteRentabilidadNetaDTO> detalle = buscarRentabilidadFiltrada(
                seleccionado.getId(), null, null, null,
                seleccionado.getFechaInicio(), seleccionado.getFechaFin(), null);

        BigDecimal totalIngresos = detalle.stream()
                .map(d -> d.getTotalIngresos() != null ? d.getTotalIngresos() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostos = detalle.stream()
                .map(d -> d.getTotalCostos() != null ? d.getTotalCostos() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rentabilidadTotal = detalle.stream()
                .map(d -> d.getRentabilidadNeta() != null ? d.getRentabilidadNeta() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResumenResponseDTO(
                seleccionado.getId(),
                seleccionado.getNombre(),
                seleccionado.getTemporada(),
                totalIngresos,
                totalCostos,
                rentabilidadTotal,
                detalle.size(),
                detalle);
    }

    private Festival buscarFestivalOExcepcion(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el festival con id " + id + "."));
    }

    private String normalizar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase(Locale.ROOT);
    }
}
