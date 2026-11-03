package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IAdminDashboardService;
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
    private final IUnidadVentaService unidadVentaService;
    private final IModificarUnidadVentaService modificarUnidadVentaService;

    public AdminController(IAdminDashboardService adminDashboardService,
                           IUnidadVentaService unidadVentaService,
                           IModificarUnidadVentaService modificarUnidadVentaService) {
        this.adminDashboardService = adminDashboardService;
        this.unidadVentaService = unidadVentaService;
        this.modificarUnidadVentaService = modificarUnidadVentaService;
    }

    @GetMapping("/dashboard")
    public String panelAdministrador(Model model) {
        model.addAllAttributes(adminDashboardService.obtenerKpisDashboard());
        return "admin/dashboard";
    }
}

