package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IReporteVentaService;
import com.example.hito2grupo3.services.IRentabilidadNetaService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasRole('RESPONSABLE')")
public class ResponsableReporteController {

    private final IReporteVentaService reporteVentaService;
    private final IRentabilidadNetaService rentabilidadNetaService;
    private final IUnidadVentaService unidadVentaService;

    public ResponsableReporteController(IReporteVentaService reporteVentaService,
                                        IRentabilidadNetaService rentabilidadNetaService,
                                        IUnidadVentaService unidadVentaService) {
        this.reporteVentaService = reporteVentaService;
        this.rentabilidadNetaService = rentabilidadNetaService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("/responsable/reportes/recaudacion")
    public String recaudacion(@RequestParam(required = false) Long festivalId,
                              Authentication authentication,
                              Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());
        List<Festival> festivales = rentabilidadNetaService.listarFestivales();

        model.addAttribute("esResponsable", true);
        model.addAttribute("festivales", festivales);
        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("ranking", Collections.emptyList());

        if (unidad == null) {
            model.addAttribute("error", "No tenés una unidad de venta asignada.");
            return "reportes/ReporteRecaudacion";
        }

        Long id = festivalId != null ? festivalId : seleccionarFestivalPorDefecto(festivales);
        model.addAttribute("festivalId", id);

        if (id == null) {
            model.addAttribute("error", "No hay festivales disponibles para consultar.");
            return "reportes/ReporteRecaudacion";
        }

        try {
            model.addAttribute("reporte", reporteVentaService.obtenerTotalesFestivalYUnidad(id, unidad.getId()));
            model.addAttribute("ranking", reporteVentaService.obtenerRankingFestivalYUnidad(id, unidad.getId()));
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        return "reportes/ReporteRecaudacion";
    }

    private Long seleccionarFestivalPorDefecto(List<Festival> festivales) {
        if (festivales == null || festivales.isEmpty()) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        Festival festival = festivales.stream()
                .filter(f -> f.getFechaInicio() != null && f.getFechaFin() != null
                        && !hoy.isBefore(f.getFechaInicio()) && !hoy.isAfter(f.getFechaFin()))
                .findFirst()
                .orElseGet(() -> festivales.stream()
                    .filter(f -> f.getFechaInicio() != null)
                    .max((first, second) -> first.getFechaInicio().compareTo(second.getFechaInicio()))
                .orElse(festivales.get(0)));
        return festival != null ? festival.getId() : null;
    }
}
