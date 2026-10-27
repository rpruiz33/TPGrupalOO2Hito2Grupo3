package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hito2grupo3.entities.Festival;

import com.example.hito2grupo3.services.IRentabilidadNetaService;

@Controller
public class RentabilidadNetaController {

    private final IRentabilidadNetaService r;

    public RentabilidadNetaController(IRentabilidadNetaService rentabilidadNetaService) {
        this.r = rentabilidadNetaService;
    }

    // 1. Endpoint para cargar la vista estándar del festival completo
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}")
    public String verRentabilidadNetaPorFestival(@PathVariable Long festivalId, Model model) {
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("ranking", Collections.emptyList());
        model.addAttribute("esAdministrador", true);

        try {
            model.addAttribute("festivales", r.listarFestivales());
        } catch (Exception ex) {
            model.addAttribute("festivales", Collections.emptyList());
            model.addAttribute("error", "No se pudo cargar la lista de festivales.");
            return "reportes/rentabilidadNeta";
        }

        try {
            model.addAttribute("reporte", r.obtenerReporteVenta(festivalId));
            model.addAttribute("ranking", r.obtenerRankingUnidades(festivalId));

            @SuppressWarnings("unchecked")
            Map<String, Object> reporteData = (Map<String, Object>) r.obtenerReporteVentaEntreFechas(festivalId, null, null);
            if (reporteData != null) {
                model.addAttribute("totalIngresos", reporteData.get("totalIngresos"));
                model.addAttribute("totalCostos", reporteData.get("totalCostos"));
                model.addAttribute("rentabilidadNeta", reporteData.get("rentabilidadNeta"));
                model.addAttribute("detalles", reporteData.get("detalles"));
            }
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage()
                    : "No se pudo obtener el reporte.");
        }

        return "reportes/rentabilidadNeta";
    }

    // 2. Endpoint separado con otra ruta exclusiva para filtrar entre fechas
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}/filtrar")
    public String verRentabilidadNetaEntreFechas(
            @PathVariable Long festivalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("ranking", Collections.emptyList());
        model.addAttribute("esAdministrador", true);

        try {
            model.addAttribute("festivales", r.listarFestivales());
        } catch (Exception ex) {
            model.addAttribute("festivales", Collections.emptyList());
            model.addAttribute("error", "No se pudo cargar la lista de festivales.");
            return "reportes/rentabilidadNeta";
        }

        try {
            model.addAttribute("reporte", r.obtenerReporteVenta(festivalId));
            model.addAttribute("ranking", r.obtenerRankingUnidades(festivalId));

            @SuppressWarnings("unchecked")
            Map<String, Object> reporteData = (Map<String, Object>) r.obtenerReporteVentaEntreFechas(festivalId, fechaDesde, fechaHasta);
            if (reporteData != null) {
                model.addAttribute("totalIngresos", reporteData.get("totalIngresos"));
                model.addAttribute("totalCostos", reporteData.get("totalCostos"));
                model.addAttribute("rentabilidadNeta", reporteData.get("rentabilidadNeta"));
                model.addAttribute("detalles", reporteData.get("detalles"));
            }
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage()
                    : "No se pudo obtener el reporte entre fechas.");
        }

        return "reportes/rentabilidadNeta";
    }

    @GetMapping("/reportes/rentabilidad")
    public String calcularRentabilidad(
            @RequestParam(required = false) Long festivalId,
            @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {

        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("esAdministrador", true);

        List<Festival> festivales = Collections.emptyList();
        try {
            festivales = r.listarFestivales();
            model.addAttribute("festivales", festivales);
        } catch (Exception ignored) {
            model.addAttribute("festivales", Collections.emptyList());
        }

        // Si se ingresaron fechas, detectar automáticamente el festival correspondiente al rango
        if (fechaDesde != null && fechaHasta != null) {
            for (Festival f : festivales) {
                if (f.getFechaInicio() != null && f.getFechaFin() != null) {
                    if ((!fechaDesde.isBefore(f.getFechaInicio()) && !fechaHasta.isAfter(f.getFechaFin()))
                            || fechaDesde.equals(f.getFechaInicio()) || fechaHasta.equals(f.getFechaFin())) {
                        festivalId = f.getId();
                        break;
                    }
                }
            }
        }

        if (festivalId == null) {
            festivalId = 1L;
        }

        model.addAttribute("festivalId", festivalId);

        try {
            model.addAttribute("reporte", r.obtenerReporteVenta(festivalId));
            model.addAttribute("ranking", r.obtenerRankingUnidades(festivalId));

            @SuppressWarnings("unchecked")
            Map<String, Object> reporteData = (Map<String, Object>) r.obtenerReporteVentaEntreFechas(festivalId, fechaDesde, fechaHasta);
            if (reporteData != null) {
                model.addAttribute("totalIngresos", reporteData.get("totalIngresos"));
                model.addAttribute("totalCostos", reporteData.get("totalCostos"));
                model.addAttribute("rentabilidadNeta", reporteData.get("rentabilidadNeta"));
                model.addAttribute("detalles", reporteData.get("detalles"));
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No se pudo obtener el reporte: " + ex.getMessage());
            model.addAttribute("detalles", Collections.emptyList());
        }

        return "reportes/rentabilidadNeta";
    }



    @GetMapping("/cierreCaja")
    public String cierreCaja() {
        return "reportes/cierreCaja";
    }

    @GetMapping("/empleadoDashboard")
    public String empleadoDashboard() {
        return "reportes/empleadoDashboard";
    }

    @GetMapping("/reciboPdf")
    public String reciboPdf() {
        return "reportes/reciboPdf";
    }
}