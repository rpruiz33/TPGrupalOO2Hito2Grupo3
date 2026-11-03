package com.example.hito2grupo3.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ICanonService;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.IRentabilidadNetaService;

@Service
public class RentabilidadNetaServiceImpl implements IRentabilidadNetaService {

    private final FestivalRepository festivalRepository;
    private final PedidoRepository pedidoRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final ILiquidacionService liquidacionService;
    private final ICanonService canonService;

    public RentabilidadNetaServiceImpl(
            FestivalRepository festivalRepository,
            PedidoRepository pedidoRepository,
            UnidadVentaRepository unidadVentaRepository,
            ILiquidacionService liquidacionService,
            ICanonService canonService) {
        this.festivalRepository = festivalRepository;
        this.pedidoRepository = pedidoRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.liquidacionService = liquidacionService;
        this.canonService = canonService;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentaDTO obtenerReporteVenta(Long festivalId) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new IllegalArgumentException("Festival con ID " + festivalId + " no encontrado.");
        }
        ReporteVentaDTO reporte = pedidoRepository.obtenerReporteVentaPorFestival(festivalId);
        if (reporte == null) {
            Festival festival = festivalRepository.findById(festivalId).orElse(null);
            String nombre = festival != null ? festival.getNombre() : "Festival " + festivalId;
            reporte = new ReporteVentaDTO(festivalId, nombre, BigDecimal.ZERO, 0L);
        }
        return reporte;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingUnidadDTO> obtenerRankingUnidades(Long festivalId) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new IllegalArgumentException("Festival con ID " + festivalId + " no encontrado.");
        }
        List<RankingUnidadDTO> ranking = pedidoRepository.obtenerRankingPorFestival(festivalId);
        return ranking != null ? ranking : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarFestivales() {
        return festivalRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadVenta> listarUnidadesPorFestival(Long festivalId) {
        if (festivalId == null) {
            return unidadVentaRepository.findAll();
        }
        Festival festival = festivalRepository.findById(festivalId).orElse(null);
        if (festival != null && festival.getUnidadesVentaHabilitadas() != null
                && !festival.getUnidadesVentaHabilitadas().isEmpty()) {
            return new ArrayList<>(festival.getUnidadesVentaHabilitadas());
        }
        return unidadVentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteRentabilidadNetaDTO calcularRentabilidadUnidad(Long festivalId, Long unidadVentaId) {
        return calcularRentabilidadUnidadEntreFechas(festivalId, unidadVentaId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteRentabilidadNetaDTO calcularRentabilidadUnidadEntreFechas(
            Long festivalId, Long unidadVentaId, LocalDate fechaDesde, LocalDate fechaHasta) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("Festival con ID " + festivalId + " no encontrado."));

        UnidadVenta unidad = unidadVentaRepository.findById(unidadVentaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidad de venta con ID " + unidadVentaId + " no encontrada."));

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        // 1. Ingresos por pedidos activos (excluyendo anulados 'R')
        BigDecimal ingresos = pedidoRepository.sumarIngresosPorFestivalYUnidadYFechas(
                festivalId, unidadVentaId, fechaDesde, fechaHasta);
        if (ingresos == null)
            ingresos = BigDecimal.ZERO;

        // 2. Costos de producción de los platos vendidos en los pedidos activos
        BigDecimal costosPlatos = pedidoRepository.sumarCostosPlatosPorFestivalYUnidadYFechas(
                festivalId, unidadVentaId, fechaDesde, fechaHasta);
        if (costosPlatos == null)
            costosPlatos = BigDecimal.ZERO;

        // 3. Sueldos del personal de la unidad según liquidación de haberes
        BigDecimal sueldos = BigDecimal.ZERO;
        try {
            ILiquidacionService.ResultadoLiquidacion liq = liquidacionService.liquidarPorUnidad(unidadVentaId,
                    festivalId);
            if (liq != null && liq.totalGeneral() != null) {
                sueldos = liq.totalGeneral();
            }
        } catch (Exception ignored) {
            sueldos = BigDecimal.ZERO;
        }

        // 4. Canon obligatorio de la unidad de venta
        BigDecimal canon = BigDecimal.ZERO;
        try {
            canon = canonService.calcularCanon(unidad);
            if (canon == null)
                canon = BigDecimal.ZERO;
        } catch (Exception ignored) {
            canon = BigDecimal.ZERO;
        }

        // 5. Total de costos = Costos de platos + Sueldos + Canon
        BigDecimal totalCostos = costosPlatos.add(sueldos).add(canon);

        // 6. Rentabilidad Neta = (Pedidos totales - Costos de platos) - Sueldos - Canon
        BigDecimal rentabilidadNeta = ingresos.subtract(totalCostos);

        // 7. Cantidad de pedidos activos
        Long cantidadPedidos = pedidoRepository.contarPedidosPorFestivalYUnidadYFechas(
                festivalId, unidadVentaId, fechaDesde, fechaHasta);
        if (cantidadPedidos == null)
            cantidadPedidos = 0L;

        String tipoUnidad = "UnidadVenta";
        if (unidad instanceof FoodTruck) {
            tipoUnidad = "FoodTruck";
        } else if (unidad instanceof PuestoDesarmable) {
            tipoUnidad = "PuestoDesarmable";
        }

        String periodoStr = formatearPeriodo(fechaDesde, fechaHasta, festival);

        return new ReporteRentabilidadNetaDTO(
                festival.getId(),
                festival.getNombre(),
                unidad.getId(),
                unidad.getNombreComercial(),
                unidad.getCodigo(),
                tipoUnidad,
                ingresos,
                costosPlatos,
                sueldos,
                canon,
                totalCostos,
                rentabilidadNeta,
                cantidadPedidos,
                periodoStr);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporteRentabilidad(
            Long festivalId, Long unidadVentaId, LocalDate fechaDesde, LocalDate fechaHasta) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("Festival no encontrado con ID: " + festivalId));

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        List<ReporteRentabilidadNetaDTO> detalles = new ArrayList<>();
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalCostosPlatos = BigDecimal.ZERO;
        BigDecimal totalSueldos = BigDecimal.ZERO;
        BigDecimal totalCanon = BigDecimal.ZERO;
        BigDecimal totalCostos = BigDecimal.ZERO;
        BigDecimal totalRentabilidadNeta = BigDecimal.ZERO;

        if (unidadVentaId != null) {
            // Rentabilidad para una unidad específica
            ReporteRentabilidadNetaDTO dto = calcularRentabilidadUnidadEntreFechas(
                    festivalId, unidadVentaId, fechaDesde, fechaHasta);
            detalles.add(dto);

            totalIngresos = dto.getTotalIngresos();
            totalCostosPlatos = dto.getCostosPlatos();
            totalSueldos = dto.getSueldos();
            totalCanon = dto.getCanon();
            totalCostos = dto.getTotalCostos();
            totalRentabilidadNeta = dto.getRentabilidadNeta();
        } else {
            // Rentabilidad para todas las unidades habilitadas del festival
            List<UnidadVenta> unidades = listarUnidadesPorFestival(festivalId);
            for (UnidadVenta uv : unidades) {
                ReporteRentabilidadNetaDTO dto = calcularRentabilidadUnidadEntreFechas(
                        festivalId, uv.getId(), fechaDesde, fechaHasta);
                detalles.add(dto);

                totalIngresos = totalIngresos.add(dto.getTotalIngresos());
                totalCostosPlatos = totalCostosPlatos.add(dto.getCostosPlatos());
                totalSueldos = totalSueldos.add(dto.getSueldos());
                totalCanon = totalCanon.add(dto.getCanon());
                totalCostos = totalCostos.add(dto.getTotalCostos());
                totalRentabilidadNeta = totalRentabilidadNeta.add(dto.getRentabilidadNeta());
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalIngresos", totalIngresos);
        resultado.put("totalCostosPlatos", totalCostosPlatos);
        resultado.put("totalSueldos", totalSueldos);
        resultado.put("totalCanon", totalCanon);
        resultado.put("totalCostos", totalCostos);
        resultado.put("rentabilidadNeta", totalRentabilidadNeta);
        resultado.put("detalles", detalles);
        resultado.put("periodo", formatearPeriodo(fechaDesde, fechaHasta, festival));

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Object obtenerReporteVentaEntreFechas(Long festivalId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return obtenerReporteRentabilidad(festivalId, null, fechaDesde, fechaHasta);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentaDTO obtenerReporteVentaEntreFechas(Long festivalId) {
        return obtenerReporteVenta(festivalId);
    }

    private String formatearPeriodo(LocalDate fechaDesde, LocalDate fechaHasta, Festival festival) {
        if (fechaDesde != null && fechaHasta != null) {
            return fechaDesde + " al " + fechaHasta;
        } else if (fechaDesde != null) {
            return "Desde " + fechaDesde;
        } else if (fechaHasta != null) {
            return "Hasta " + fechaHasta;
        } else if (festival != null && festival.getFechaInicio() != null && festival.getFechaFin() != null) {
            return festival.getFechaInicio() + " al " + festival.getFechaFin();
        }
        return "Período completo";
    }
}