package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
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

    private static final String VISTA_RENTABILIDAD = "reportes/rentabilidadNeta";

    private final IRentabilidadNetaService r;

    public RentabilidadNetaController(IRentabilidadNetaService rentabilidadNetaService) {
        this.r = rentabilidadNetaService;
    }

    // 1. Vista estándar del festival completo
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}")
    public String verRentabilidadNetaPorFestival(@PathVariable Long festivalId, Model model) {
        return cargarReporte(festivalId, null, null, model);
    }

    // 2. Filtrado entre fechas
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}/filtrar")
    public String verRentabilidadNetaEntreFechas(
            @PathVariable Long festivalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        return cargarReporte(festivalId, fechaDesde, fechaHasta, model);
    }

    // 3. Acción del formulario: todos los parámetros son opcionales
    @GetMapping("/reportes/rentabilidad")
    public String calcularRentabilidad(
            @RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        return cargarReporte(festivalId, fechaDesde, fechaHasta, model);
    }

    /**
     * Lógica común: siempre carga festivales, ranking y KPIs con datos reales.
     * - Sin festival: usa el que está en curso, o el más reciente.
     * - Sin fechas: usa las del festival.
     */
    private String cargarReporte(Long festivalId, LocalDate desde, LocalDate hasta, Model model) {
        model.addAttribute("esAdministrador", true);
        model.addAttribute("ranking", Collections.emptyList());
        model.addAttribute("detalles", Collections.emptyList());

        List<Festival> festivales;
        try {
            festivales = r.listarFestivales();
        } catch (Exception ex) {
            model.addAttribute("festivales", Collections.emptyList());
            model.addAttribute("error", "No se pudo cargar la lista de festivales.");
            return VISTA_RENTABILIDAD;
        }
        model.addAttribute("festivales", festivales);

        // 1) Festival por defecto
        Long id = festivalId;
        if (id == null && !festivales.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            Festival defecto = festivales.stream()
                    .filter(f -> !hoy.isBefore(f.getFechaInicio()) && !hoy.isAfter(f.getFechaFin()))
                    .findFirst()
                    .orElseGet(() -> festivales.stream()
                            .max(Comparator.comparing(Festival::getFechaInicio)).get());
            id = defecto.getId();
        }

        // 2) Fechas por defecto: las del festival
        final Long idFinal = id;
        Festival fest = festivales.stream()
                .filter(f -> f.getId().equals(idFinal))
                .findFirst()
                .orElse(null);
        if (fest != null) {
            if (desde == null) desde = fest.getFechaInicio();
            if (hasta == null) hasta = fest.getFechaFin();
        }

        model.addAttribute("festivalId", id);
        model.addAttribute("fechaDesde", desde);
        model.addAttribute("fechaHasta", hasta);

        if (id == null) {
            return VISTA_RENTABILIDAD;
        }

        // Validación de rango de fechas
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            model.addAttribute("error", "La fecha 'Desde' (" + desde + ") no puede ser posterior a la fecha 'Hasta' (" + hasta + ").");
            return VISTA_RENTABILIDAD;
        }

        // 3) Calcular siempre
        try {
            model.addAttribute("ranking", r.obtenerRankingUnidades(id));

            @SuppressWarnings("unchecked")
            Map<String, Object> data =
                    (Map<String, Object>) r.obtenerReporteVentaEntreFechas(id, desde, hasta);
            if (data != null) {
                model.addAttribute("totalIngresos", data.get("totalIngresos"));
                model.addAttribute("totalCostos", data.get("totalCostos"));
                model.addAttribute("rentabilidadNeta", data.get("rentabilidadNeta"));
                model.addAttribute("detalles", data.get("detalles"));
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No se pudo obtener el reporte: " + ex.getMessage());
        }

        return VISTA_RENTABILIDAD;
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