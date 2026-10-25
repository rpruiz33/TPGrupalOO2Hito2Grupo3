package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
            model.addAttribute("reporte", r.obtenerReporteVentaEntreFechas(festivalId, fechaDesde, fechaHasta));
            model.addAttribute("ranking", r.obtenerRankingUnidades(festivalId));
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage()
                    : "No se pudo obtener el reporte entre fechas.");
        }

        return "reportes/rentabilidadNeta";
    }

   @GetMapping("/reportes/rentabilidad")
    public String calcularRentabilidad(
            @RequestParam(required = false, defaultValue = "1") Long festivalId,
            @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam("fechaHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {

        model.addAttribute("festivalId", festivalId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("esAdministrador", true);

        try {
            // Llamamos al servicio que nos devuelve el Map con los cálculos
            Map<String, Object> reporteData = (Map<String, Object>) r.obtenerReporteVentaEntreFechas(festivalId, fechaDesde, fechaHasta);

            // Inyectamos las variables exactas que espera tu HTML
            model.addAttribute("totalIngresos", reporteData.get("totalIngresos"));
            model.addAttribute("totalCostos", reporteData.get("totalCostos"));
            model.addAttribute("rentabilidadNeta", reporteData.get("rentabilidadNeta"));
            model.addAttribute("detalles", reporteData.get("detalles"));

        } catch (Exception ex) {
            model.addAttribute("error", "No se pudo obtener el reporte: " + ex.getMessage());
            model.addAttribute("detalles", Collections.emptyList()); // Evita que sea null
        }

        return "reportes/rentabilidadNeta"; // Asegúrate de que coincida con la ruta de tu HTML
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