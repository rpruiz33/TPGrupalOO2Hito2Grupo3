package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.IReporteVentaService;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.hito2grupo3.dto.ReporteMayoresCanonDTO;
import com.example.hito2grupo3.services.IReporteMayoresCanonService;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    @Autowired
    private IReporteVentaService reporteVentaService;
    @Autowired
    private IReporteMayoresCanonService reporteMayoresCanonService;
    @Autowired
    private IRentabilidadNetaService rentabilidadNetaService;
    @Autowired
    private FestivalRepository festivalRepository;

    /**
     * Punto de entrada del reporte de recaudación para Admin.
     * Si no viene festivalId, redirige al festival por defecto.
     */
    @GetMapping("/recaudacion")
    public String recaudacionAdmin(@RequestParam(required = false) Long festivalId, Model model) {
        List<Festival> festivales = festivalRepository.findAll();
        Long id = festivalId;
        if (id == null && !festivales.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            Festival defecto = festivales.stream()
                    .filter(f -> f.getFechaInicio() != null && f.getFechaFin() != null
                            && !hoy.isBefore(f.getFechaInicio()) && !hoy.isAfter(f.getFechaFin()))
                    .findFirst()
                    .orElseGet(() -> festivales.stream()
                            .filter(f -> f.getFechaInicio() != null)
                            .max(Comparator.comparing(Festival::getFechaInicio))
                            .orElse(festivales.get(0)));
            id = defecto != null ? defecto.getId() : null;
        }
        return cargarVistaRecaudacion(id, festivales, model);
    }

    @GetMapping("/recaudacion/festival/{festivalId}")
    public String verReporteRecaudacion(@PathVariable Long festivalId, Model model) {
        List<Festival> festivales = festivalRepository.findAll();
        return cargarVistaRecaudacion(festivalId, festivales, model);
    }

    private String cargarVistaRecaudacion(Long festivalId, List<Festival> festivales, Model model) {
        model.addAttribute("esResponsable", false);
        model.addAttribute("festivales", festivales != null ? festivales : Collections.emptyList());
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("ranking", Collections.emptyList());

        if (festivalId == null) {
            model.addAttribute("error", "No hay festivales disponibles. Creá uno primero.");
            return "reportes/ReporteRecaudacion";
        }

        try {
            ReporteVentaDTO reporte = reporteVentaService.obtenerTotalesFestival(festivalId);
            model.addAttribute("reporte", reporte);
        } catch (RuntimeException e) {
            // El festival existe pero no tiene pedidos aún — mostrar vacío
            Festival fest = festivalRepository.findById(festivalId).orElse(null);
            if (fest != null) {
                model.addAttribute("reporte",
                        new com.example.hito2grupo3.dto.ReporteVentaDTO(festivalId, fest.getNombre(),
                                java.math.BigDecimal.ZERO, 0L));
            } else {
                model.addAttribute("error", "El festival seleccionado no existe.");
                return "reportes/ReporteRecaudacion";
            }
        }

        try {
            List<RankingUnidadDTO> ranking = reporteVentaService.obtenerRankingFestival(festivalId);
            model.addAttribute("ranking", ranking != null ? ranking : Collections.emptyList());
        } catch (RuntimeException e) {
            model.addAttribute("ranking", Collections.emptyList());
        }

        return "reportes/ReporteRecaudacion";
    }

    @GetMapping("/mayores-canon")
    public String verReporteMayoresCanon(
            @RequestParam(defaultValue = "3") int cantidad,
            Model model) {

        model.addAttribute("cantidad", cantidad);

        try {
            List<ReporteMayoresCanonDTO> reporte = reporteMayoresCanonService.obtenerMayoresCanon(cantidad);

            model.addAttribute("reporte", reporte);
            return "reportes/ReporteMayoresCanon";

        } catch (IllegalStateException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("reporte", List.of());
            return "reportes/ReporteMayoresCanon";

        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("reporte", List.of());
            return "reportes/ReporteMayoresCanon";
        }
}
}