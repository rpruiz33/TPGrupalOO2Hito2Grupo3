package com.example.hito2grupo3.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Costos;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.services.IRentabilidadNetaService;

import lombok.Getter;


@Service
public class RentabilidadNetaServiceImpl implements IRentabilidadNetaService {

    private final FestivalRepository festivalRepository;
    private final PedidoRepository pedidoRepository;

    // Inyección de dependencias mediante constructor explícito (la forma más segura en Spring Boot)
    public RentabilidadNetaServiceImpl(FestivalRepository festivalRepository, PedidoRepository pedidoRepository) {
        this.festivalRepository = festivalRepository;
        this.pedidoRepository = pedidoRepository;
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

   @Override
    @Transactional(readOnly = true)
    public Object obtenerReporteVentaEntreFechas(Long festivalId, LocalDate fechaDesde, LocalDate fechaHasta) {
        // 1. Buscar el festival
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new RuntimeException("Festival no encontrado con ID: " + festivalId));

        // 2. Validar fechas
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        // 3. Obtener ingresos reales filtrados por fecha (si se proporcionan) o totales del festival
        Double totalIngresos = pedidoRepository.sumarIngresosPorFestivalYFechas(festivalId, fechaDesde, fechaHasta);
        if (totalIngresos == null) {
            totalIngresos = 0.0;
        }

        // 4. Calcular costos operativos desde la entidad Festival (relación @OneToMany de Costos)
        double totalCostos = 0.0;
        if (festival.getCostos() != null) {
            for (Costos c : festival.getCostos()) {
                // Asegúrate de usar el método correcto de tu entidad Costos (ej. getMonto() o getValor())
                if (c.getMonto() != null) {
                    totalCostos += c.getMonto().doubleValue();
                }
            }
        }

        // 5. Calcular Rentabilidad Neta
        double rentabilidadNeta = totalIngresos - totalCostos;

        // 6. Formatear el periodo de forma segura
        String periodoStr = "Fechas no especificadas";
        if (fechaDesde != null && fechaHasta != null) {
            periodoStr = fechaDesde.toString() + " al " + fechaHasta.toString();
        } else if (fechaDesde != null) {
            periodoStr = "Desde el " + fechaDesde.toString();
        } else if (fechaHasta != null) {
            periodoStr = "Hasta el " + fechaHasta.toString();
        }

        // 7. Crear el detalle para la tabla de Thymeleaf
        List<Map<String, Object>> detalles = new ArrayList<>();
        Map<String, Object> detalleItem = new HashMap<>();
        detalleItem.put("concepto", festival.getNombre());
        detalleItem.put("periodo", periodoStr);
        detalleItem.put("ingresos", totalIngresos);
        detalleItem.put("costos", totalCostos);
        detalleItem.put("gananciaNeta", rentabilidadNeta);
        detalles.add(detalleItem);

        // 8. Empaquetar todo en el mapa de resultados
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalIngresos", totalIngresos);
        resultado.put("totalCostos", totalCostos);
        resultado.put("rentabilidadNeta", rentabilidadNeta);
        resultado.put("detalles", detalles);

        return resultado;
    }

    @Override
    public ReporteVentaDTO obtenerReporteVentaEntreFechas(Long festivalId) {
        return obtenerReporteVenta(festivalId);
    }
}