package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IAdminDashboardService;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.IModificarUnidadVentaService;
import com.example.hito2grupo3.services.IUnidadVentaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final IAdminDashboardService adminDashboardService;
    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;
    private final IModificarUnidadVentaService modificarUnidadVentaService;

    public AdminController(IAdminDashboardService adminDashboardService,
                           IFestivalService festivalService,
                           IUnidadVentaService unidadVentaService,
                           IModificarUnidadVentaService modificarUnidadVentaService) {
        this.adminDashboardService = adminDashboardService;
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
        this.modificarUnidadVentaService = modificarUnidadVentaService;
    }

    @GetMapping("/dashboard")
    public String panelAdministrador(Model model) {
        model.addAllAttributes(adminDashboardService.obtenerKpisDashboard());
        return "admin/dashboard";
    }

    @GetMapping("/unidades/{id}/editar")
    public String mostrarFormularioEdicion(
            @PathVariable Long id,
            @RequestParam Long festivalId,
            Model model) {

        UnidadVenta unidad = unidadVentaService.buscarPorId(id);

        if (unidad == null) {
            return "redirect:/admin/festivales";
        }

        model.addAttribute("unidad", unidad);
        model.addAttribute("esFoodTruck", unidad instanceof FoodTruck);
        model.addAttribute("festivalId", festivalId);

        return "admin/modificar-unidad";
    }

    @PostMapping("/unidades/{id}/editar")
    public String modificarUnidad(
            @PathVariable Long id,
            @RequestParam String nombreComercial,
            @RequestParam BigDecimal superficieM2,
            @RequestParam(required = false) String patente,
            @RequestParam(required = false) Boolean requiereElectricidad,
            @RequestParam(required = false) Integer cantidadCarpas,
            @RequestParam(required = false) Integer tiempoMontajeMin,
            @RequestParam(required = false) Long festivalId,
            RedirectAttributes redirectAttributes) {

        try {
            modificarUnidadVentaService.modificar(
                    id,
                    nombreComercial,
                    superficieM2,
                    patente,
                    requiereElectricidad,
                    cantidadCarpas,
                    tiempoMontajeMin);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "La unidad de venta se modificó correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            if (festivalId != null) {
                return "redirect:/admin/unidades/" + id
                        + "/editar?festivalId=" + festivalId;
            }

            return "redirect:/admin/festivales";
        }

        if (festivalId != null) {
            return "redirect:/admin/festivales/" + festivalId;
        }

        return "redirect:/admin/festivales";
    }

    @PostMapping("/unidades/{id}/baja")
    public String darDeBajaUnidad(
            @PathVariable Long id,
            @RequestParam Long festivalId,
            RedirectAttributes redirectAttributes) {

        try {
            festivalService.desasociarUnidadDeFestival(festivalId, id);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                "La unidad de venta se quitó de este festival correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/admin/festivales/" + festivalId;
    }
}
