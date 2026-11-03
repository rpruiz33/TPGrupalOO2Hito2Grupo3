package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.services.IEmpleadoInfoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.hito2grupo3.dto.AsignacionStaffDTO;
import java.util.List;
import java.security.Principal;
import java.util.Map;

@Controller
public class EmpleadoController {

    private final IEmpleadoInfoService empleadoInfoService;

    public EmpleadoController(IEmpleadoInfoService empleadoInfoService) {
        this.empleadoInfoService = empleadoInfoService;
    }

@GetMapping("/empleado/index")
public String mostrarIndex(
        Principal principal,
        Authentication authentication,
        Model model) {

    if (principal == null || authentication == null) {
        return "redirect:/login";
    }

    try {
        Map<String, Object> dashboardData =
                empleadoInfoService.obtenerDashboardEmpleado(principal.getName());

        model.addAllAttributes(dashboardData);

    } catch (Exception e) {
        model.addAttribute(
                "error",
                "No se pudo cargar la información del personal."
        );
    }

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

    @GetMapping("/empleado/historial-asignaciones")
public String mostrarHistorial(
        Principal principal,
        Authentication authentication,
        Model model) {

    if (principal == null || authentication == null) {
        return "redirect:/login";
    }

    boolean esEmpleadoAutorizado =
            tieneRol(authentication, "ROLE_EMPLEADO")
            || tieneRol(authentication, "ROLE_CAJERO")
            || tieneRol(authentication, "ROLE_COCINERO");

    if (!esEmpleadoAutorizado) {
        return "redirect:/access-denied";
    }

    try {
        List<AsignacionStaffDTO> historial =
                empleadoInfoService.obtenerHistorialAsignaciones(principal.getName());

        model.addAttribute("historialAsignaciones", historial);

    } catch (Exception e) {
        model.addAttribute("error", "No se pudo cargar el historial de asignaciones.");
    }

    return "historial-asignaciones";
}
    
}
