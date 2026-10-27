package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.CierreCaja;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ICierreCajaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cajero")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CAJERO')")
public class CajeroController {

    private final ICierreCajaService cierreCajaService;
    private final UnidadVentaRepository unidadVentaRepository;

    public CajeroController(ICierreCajaService cierreCajaService,
                            UnidadVentaRepository unidadVentaRepository) {
        this.cierreCajaService = cierreCajaService;
        this.unidadVentaRepository = unidadVentaRepository;
    }

@GetMapping("/index")
public String mostrarIndex(Model model, Principal principal) {
    model.addAttribute("esCajero", true);
    return "cajero/Cajero-index";
}

    /**
     * GET /cajero/cierre-caja
     * Muestra la vista de cierre de caja con el historial del cajero logueado.
     */
    @GetMapping("/cierre-caja")
    public String mostrarCierreCaja(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Principal principal,
            Model model) {

        String email = principal != null ? principal.getName() : "cajero@epicentro.com";

        try {
            Map<String, Object> resumen = cierreCajaService.obtenerResumenCierre(email);
            model.addAllAttributes(resumen);

            // Si se especificaron fechas, filtrar el historial
            if (desde != null && hasta != null) {
                Long cajeroId = (Long) resumen.get("cajeroId");
                if (cajeroId != null) {
                    List<CierreCaja> historialFiltrado =
                            cierreCajaService.obtenerHistorialPorCajeroYFechas(cajeroId, desde, hasta);
                    BigDecimal totalFiltrado =
                            cierreCajaService.calcularRecaudacionEntreFechas(cajeroId, desde, hasta);
                    model.addAttribute("historial", historialFiltrado);
                    model.addAttribute("totalFiltrado", totalFiltrado);
                }
                model.addAttribute("desde", desde);
                model.addAttribute("hasta", hasta);
            }
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo cargar la información del cierre de caja: " + e.getMessage());
            model.addAttribute("historial", Collections.emptyList());
            model.addAttribute("totalAcumulado", BigDecimal.ZERO);
            model.addAttribute("cajeroNombre", "Cajero");
            model.addAttribute("fechaHoy", LocalDate.now());
        }

        // Cargar unidades de venta para el formulario de registro
        try {
            List<UnidadVenta> unidades = unidadVentaRepository.findAll();
            model.addAttribute("unidades", unidades);
        } catch (Exception e) {
            model.addAttribute("unidades", Collections.emptyList());
        }

        return "cajero/cierreCaja";
    }

    /**
     * POST /cajero/cierre-caja
     * Registra un nuevo cierre de caja.
     */
    @PostMapping("/cierre-caja")
    public String registrarCierreCaja(
            @RequestParam Long cajeroId,
            @RequestParam Long unidadVentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) BigDecimal montoRecaudado,
            RedirectAttributes redirectAttributes) {

        try {
            cierreCajaService.registrarCierre(cajeroId, unidadVentaId, fecha, montoRecaudado);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Cierre de caja registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar el cierre de caja: " + e.getMessage());
        }

        return "redirect:/cajero/cierre-caja";
    }
}
