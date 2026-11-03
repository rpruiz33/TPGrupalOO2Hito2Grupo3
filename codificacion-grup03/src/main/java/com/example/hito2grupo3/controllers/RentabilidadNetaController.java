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
import com.example.hito2grupo3.entities.UnidadVenta;
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
        return cargarReporte(festivalId, null, null, null, model);
    }

    // 2. Filtrado entre fechas y unidad opcional
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}/filtrar")
    public String verRentabilidadNetaEntreFechas(
            @PathVariable Long festivalId,
            @RequestParam(required = false) Long unidadVentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        return cargarReporte(festivalId, unidadVentaId, fechaDesde, fechaHasta, model);
    }

    // 3. Acción del formulario: todos los parámetros son opcionales
    @GetMapping("/reportes/rentabilidad")
    public String calcularRentabilidad(
            @RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) Long unidadVentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        return cargarReporte(festivalId, unidadVentaId, fechaDesde, fechaHasta, model);
    }

    // 4. Punto 6 del TP: Cálculo de rentabilidad neta de una unidad en un festival
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}/unidad/{unidadVentaId}")
    public String verRentabilidadNetaPorUnidad(
            @PathVariable Long festivalId,
            @PathVariable Long unidadVentaId,
            Model model) {
        return cargarReporte(festivalId, unidadVentaId, null, null, model);
    }

    // 5. Punto 7 del TP: Cálculo de rentabilidad neta para un festival y una unidad entre dos fechas
    @GetMapping("/admin/reportes/rentabilidad-neta/festival/{festivalId}/unidad/{unidadVentaId}/filtrar")
    public String verRentabilidadNetaUnidadEntreFechas(
            @PathVariable Long festivalId,
            @PathVariable Long unidadVentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        return cargarReporte(festivalId, unidadVentaId, fechaDesde, fechaHasta, model);
    }

    /**
     * Lógica común: carga festivales, unidades, ranking y KPIs con datos financieros reales.
     * Descuenta obligatoriamente:
     * - Costos de platos (producción)
     * - Sueldos del personal asignado
     * - Canon de la unidad de venta
     */
    private String cargarReporte(Long festivalId, Long unidadVentaId, LocalDate desde, LocalDate hasta, Model model) {
        model.addAttribute("esAdministrador", true);
        model.addAttribute("ranking", Collections.emptyList());
        model.addAttribute("detalles", Collections.emptyList());
        model.addAttribute("unidades", Collections.emptyList());

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

        // Cargar unidades del festival seleccionado
        List<UnidadVenta> unidadesFestival = Collections.emptyList();
        if (id != null) {
            try {
                unidadesFestival = r.listarUnidadesPorFestival(id);
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("unidades", unidadesFestival);

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
        model.addAttribute("unidadVentaId", unidadVentaId);
        model.addAttribute("fechaDesde", desde);
        model.addAttribute("fechaHasta", hasta);

        if (id == null) {
            return VISTA_RENTABILIDAD;
        }

        // Validación de coherencia de rango de fechas
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            model.addAttribute("error", "La fecha 'Desde' (" + desde + ") no puede ser posterior a la fecha 'Hasta' (" + hasta + ").");
            return VISTA_RENTABILIDAD;
        }

        // Validación de límites del festival seleccionado
        if (fest != null && fest.getFechaInicio() != null && fest.getFechaFin() != null) {
            if (desde != null && desde.isBefore(fest.getFechaInicio())) {
                model.addAttribute("error", "La fecha 'Desde' (" + desde + ") no puede ser anterior al inicio del festival (" + fest.getFechaInicio() + ").");
                return VISTA_RENTABILIDAD;
            }
            if (hasta != null && hasta.isAfter(fest.getFechaFin())) {
                model.addAttribute("error", "La fecha 'Hasta' (" + hasta + ") no puede ser posterior al cierre del festival (" + fest.getFechaFin() + ").");
                return VISTA_RENTABILIDAD;
            }
        }

        // 3) Calcular rentabilidad neta real (con pedidos - costoPlatos - sueldos - canon)
        try {
            model.addAttribute("ranking", r.obtenerRankingUnidades(id));

            Map<String, Object> data = r.obtenerReporteRentabilidad(id, unidadVentaId, desde, hasta);
            if (data != null) {
                model.addAttribute("totalIngresos", data.get("totalIngresos"));
                model.addAttribute("totalCostosPlatos", data.get("totalCostosPlatos"));
                model.addAttribute("totalSueldos", data.get("totalSueldos"));
                model.addAttribute("totalCanon", data.get("totalCanon"));
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