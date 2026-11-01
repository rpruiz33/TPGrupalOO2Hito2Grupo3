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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;

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
            Authentication authentication,
            Principal principal,
            Model model) {

        String email = principal != null ? principal.getName() : "cajero@epicentro.com";

        try {
            Map<String, Object> resumen = cierreCajaService.obtenerResumenCierre(email);
            model.addAllAttributes(resumen);

            // EXTRAER Y PASAR EXPLÍCITAMENTE LOS IDs AL MODELO PARA EL FORMULARIO
            if (resumen != null) {
                Long cajeroId = (Long) resumen.get("cajeroId");
                model.addAttribute("cajeroId", cajeroId);

                Long unidadVentaId = (Long) resumen.get("unidadVentaId");
                model.addAttribute("unidadVentaId", unidadVentaId);
            }

            // Si se especificaron fechas, validar rango y filtrar el historial
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

        // Cargar unidades de venta: el cajero solo debe ver sus unidades asignadas
        try {
            Long cajeroId = (Long) model.getAttribute("cajeroId");
            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            List<UnidadVenta> unidades;
            if (esAdmin) {
                unidades = unidadVentaRepository.findAll();
            } else if (cajeroId != null) {
                unidades = new ArrayList<>(unidadVentaRepository.buscarPorStaffAsignado(cajeroId));
                if (unidades.isEmpty()) {
                    unidadVentaRepository.findByResponsable_Id(cajeroId).ifPresent(unidades::add);
                }
            } else {
                unidades = Collections.emptyList();
            }

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
     * Registra un nuevo cierre de caja con validaciones de negocio.
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

        try {
            String email = principal != null ? principal.getName() : "cajero@epicentro.com";
            Map<String, Object> resumen = null;

            try {
                resumen = cierreCajaService.obtenerResumenCierre(email);
            } catch (Exception ignored) {}

            // 1. Resolver cajeroId si viene nulo
            if (cajeroId == null && resumen != null) {
                if (resumen.get("cajeroId") instanceof Long) cajeroId = (Long) resumen.get("cajeroId");
                else if (resumen.get("cajeroId") != null) cajeroId = Long.valueOf(resumen.get("cajeroId").toString());
            }

            if (cajeroId == null) {
                throw new IllegalArgumentException("No se encontró un ID válido para el cajero actual.");
            }

            // 2. Resolver unidadVentaId si viene nulo
            if (unidadVentaId == null && resumen != null && resumen.get("unidadVentaId") != null) {
                unidadVentaId = Long.valueOf(resumen.get("unidadVentaId").toString());
            }

            if (unidadVentaId == null) {
                throw new IllegalArgumentException("Debe seleccionar una unidad de venta obligatoriamente.");
            }

            // Validar que el cajero pertenezca a la unidad de venta si no es admin
            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!esAdmin) {
                final Long finalCajeroId = cajeroId;
                final Long finalUvId = unidadVentaId;
                boolean asignado = unidadVentaRepository.buscarPorStaffAsignado(finalCajeroId).stream()
                        .anyMatch(u -> Objects.equals(u.getId(), finalUvId))
                        || unidadVentaRepository.findByResponsable_Id(finalCajeroId)
                        .map(u -> Objects.equals(u.getId(), finalUvId)).orElse(false);

                if (!asignado) {
                    throw new IllegalArgumentException("No tiene permisos para registrar el cierre de una unidad de venta que no tiene asignada.");
                }
            }

            // 3. Validar fecha (no puede ser futura)
            if (fecha == null) {
                fecha = LocalDate.now();
            } else if (fecha.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de cierre de caja no puede ser una fecha futura.");
            }

            // 4. Validar monto recaudado (no negativo, máximo 2 decimales)
            if (montoRecaudado == null) {
                montoRecaudado = BigDecimal.ZERO;
            } else if (montoRecaudado.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El monto recaudado no puede ser un valor negativo.");
            } else if (montoRecaudado.scale() > 2) {
                throw new IllegalArgumentException("El monto recaudado no puede tener más de 2 decimales.");
            }

            // Ejecutar el servicio de guardado
            cierreCajaService.registrarCierre(cajeroId, unidadVentaId, fecha, montoRecaudado);
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

        try {
            String email = principal != null ? principal.getName() : "";
            Map<String, Object> resumen = null;
            try {
                resumen = cierreCajaService.obtenerResumenCierre(email);
            } catch (Exception ignored) {}

            if (cajeroId == null && resumen != null && resumen.get("cajeroId") != null) {
                cajeroId = Long.valueOf(resumen.get("cajeroId").toString());
            }
            if (unidadVentaId == null && resumen != null && resumen.get("unidadVentaId") != null) {
                unidadVentaId = Long.valueOf(resumen.get("unidadVentaId").toString());
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