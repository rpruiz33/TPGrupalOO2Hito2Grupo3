package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.services.IEmpleadoInfoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Map;

@Controller
public class EmpleadoController {

    private final IEmpleadoInfoService empleadoInfoService;

    public EmpleadoController(IEmpleadoInfoService empleadoInfoService) {
        this.empleadoInfoService = empleadoInfoService;
    }

    @GetMapping("/empleado/index")
    public String mostrarIndex() {
        return "empleado/index";
    }

    @GetMapping("/mi-informacion")
    public String mostrarDashboard(
            Principal principal,
            Authentication authentication,
            Model model) {

        if (principal == null || authentication == null) {
            return "redirect:/login";
        }

        String email = principal.getName();

        try {
            Map<String, Object> dashboardData = empleadoInfoService.obtenerDashboardEmpleado(email);
            model.addAllAttributes(dashboardData);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la información: " + e.getMessage());
        }

        return "mi-informacion";
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(rol));
    }

    @GetMapping("/empleado/volver-al-menu")
    public String volverAlMenu(
            Principal principal,
            Authentication authentication) {

        if (principal == null || authentication == null) {
            return "redirect:/login";
        }

        if (tieneRol(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        if (tieneRol(authentication, "ROLE_RESPONSABLE")) {
            return "redirect:/responsable/index";
        }

        if (tieneRol(authentication, "ROLE_CAJERO")) {
            return "redirect:/cajero/index";
        }

        if (tieneRol(authentication, "ROLE_COCINERO")
                || tieneRol(authentication, "ROLE_EMPLEADO")) {
            return "redirect:/empleado/index";
        }

        return "redirect:/access-denied";
    }
}
