package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.CierreCaja;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.ICierreCajaService;
import com.example.hito2grupo3.services.IEmpleadoInfoService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import java.util.Objects;



@Controller
@RequestMapping("/cajero")
@PreAuthorize("hasRole('CAJERO')")
public class CajeroController {

private final ICierreCajaService cierreCajaService;
private final IEmpleadoInfoService empleadoInfoService;

public CajeroController(
        ICierreCajaService cierreCajaService,
        IEmpleadoInfoService empleadoInfoService) {
    this.cierreCajaService = cierreCajaService;
    this.empleadoInfoService = empleadoInfoService;
}

@GetMapping("/index")
public String mostrarIndex(Model model, Principal principal) {

    model.addAttribute("esCajero", true);

    if (principal == null) {
        return "redirect:/login";
    }

    try {
        Map<String, Object> dashboardData =
                empleadoInfoService.obtenerDashboardEmpleado(principal.getName());

        model.addAttribute("unidadVenta", dashboardData.get("unidadAsignada"));
        model.addAttribute("asignacionActual", dashboardData.get("asignacionActual"));

    } catch (Exception e) {
        model.addAttribute("unidadVenta", null);
        model.addAttribute("asignacionActual", null);
        model.addAttribute(
                "error",
                "No se pudo cargar la información de la unidad de venta."
        );
    }

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
            Authentication authentication,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();

        try {
            Map<String, Object> resumen = cierreCajaService.obtenerResumenCierre(email);
            model.addAllAttributes(resumen);

            // Pasar explícitamente los IDs al modelo para el formulario
            if (resumen != null) {
                Long cajeroId = (Long) resumen.get("cajeroId");
                model.addAttribute("cajeroId", cajeroId);

                Long unidadVentaId = (Long) resumen.get("unidadVentaId");
                model.addAttribute("unidadVentaId", unidadVentaId);
            }

            // Si se especificaron fechas, filtrar el historial
            if (desde != null && hasta != null) {
                if (desde.isAfter(hasta)) {
                    model.addAttribute("error", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
                } else {
                    Long cajeroId = (Long) resumen.get("cajeroId");
                    if (cajeroId != null) {
                        List<CierreCaja> historialFiltrado =
                                cierreCajaService.obtenerHistorialPorCajeroYFechas(cajeroId, desde, hasta);
                        BigDecimal totalFiltrado =
                                cierreCajaService.calcularRecaudacionEntreFechas(cajeroId, desde, hasta);
                        model.addAttribute("historial", historialFiltrado);
                        model.addAttribute("totalFiltrado", totalFiltrado);
                    }
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
            model.addAttribute("cajeroId", null);
            model.addAttribute("unidadVentaId", null);
        }

        // Cargar unidades de venta delegando al servicio
        try {
            Long cajeroId = (Long) model.getAttribute("cajeroId");
            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            List<UnidadVenta> unidades = cierreCajaService.resolverUnidadesDelCajero(cajeroId, esAdmin);
            model.addAttribute("unidades", unidades);

            // Si hay unidades asignadas, fijar la primera si no hay una válida seleccionada
            if (!unidades.isEmpty()) {
                Long currentUvId = (Long) model.getAttribute("unidadVentaId");
                boolean contiene = currentUvId != null && unidades.stream().anyMatch(u -> Objects.equals(u.getId(), currentUvId));
                if (!contiene) {
                    model.addAttribute("unidadVentaId", unidades.get(0).getId());
                    model.addAttribute("unidadVentaNombre", unidades.get(0).getNombreComercial());
                }
            }
        } catch (Exception e) {
            model.addAttribute("unidades", Collections.emptyList());
        }

        return "cajero/cierre-caja";
    }

    /**
     * POST /cajero/cierre-caja
     * Registra un nuevo cierre de caja delegando validaciones y persistencia al servicio.
     */
    @PostMapping("/cierre-caja")
    public String registrarCierreCaja(
            @RequestParam(required = false) Long cajeroId,
            @RequestParam(required = false) Long unidadVentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) BigDecimal montoRecaudado,
            Authentication authentication,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String email = principal.getName();

            // Si vienen nulos, resolverlos con el resumen
            if (cajeroId == null || unidadVentaId == null) {
                try {
                    Map<String, Object> resumen = cierreCajaService.obtenerResumenCierre(email);
                    if (resumen != null) {
                        if (cajeroId == null && resumen.get("cajeroId") != null) {
                            cajeroId = Long.valueOf(resumen.get("cajeroId").toString());
                        }
                        if (unidadVentaId == null && resumen.get("unidadVentaId") != null) {
                            unidadVentaId = Long.valueOf(resumen.get("unidadVentaId").toString());
                        }
                    }
                } catch (Exception ignored) {}
            }

            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            cierreCajaService.validarYRegistrarCierre(cajeroId, unidadVentaId, fecha, montoRecaudado, esAdmin);
            redirectAttributes.addFlashAttribute("mensaje", "Cierre de caja registrado exitosamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el cierre de caja: " + e.getMessage());
        }

        return "redirect:/cajero/cierre-caja";
    }

    /**
     * POST /cajero/reabrir-caja
     * Reabre el cierre de caja del día actual para permitir seguir registrando ventas.
     */
    @PostMapping("/reabrir-caja")
    public String reabrirCaja(
            @RequestParam(required = false) Long cajeroId,
            @RequestParam(required = false) Long unidadVentaId,
            @RequestParam(required = false, defaultValue = "") String motivo,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String email = principal.getName();
            if (cajeroId == null || unidadVentaId == null) {
                try {
                    Map<String, Object> resumen = cierreCajaService.obtenerResumenCierre(email);
                    if (resumen != null) {
                        if (cajeroId == null && resumen.get("cajeroId") != null) {
                            cajeroId = Long.valueOf(resumen.get("cajeroId").toString());
                        }
                        if (unidadVentaId == null && resumen.get("unidadVentaId") != null) {
                            unidadVentaId = Long.valueOf(resumen.get("unidadVentaId").toString());
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (cajeroId == null) {
                throw new IllegalArgumentException("No se pudo determinar el cajero actual.");
            }
            if (unidadVentaId == null) {
                throw new IllegalArgumentException("No se pudo determinar la unidad de venta.");
            }

            cierreCajaService.reabrirCaja(cajeroId, unidadVentaId, LocalDate.now(), motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Caja reabierta exitosamente. Podés continuar registrando ventas.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al reabrir la caja: " + e.getMessage());
        }

        return "redirect:/cajero/cierre-caja";
    }
}