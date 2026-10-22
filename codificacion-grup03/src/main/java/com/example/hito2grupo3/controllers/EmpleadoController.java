package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    final StaffRepository staffRepository;
    final UnidadVentaRepository unidadVentaRepository;
    final FestivalRepository festivalRepository;

    public EmpleadoController(StaffRepository staffRepository,
                              UnidadVentaRepository unidadVentaRepository,
                              FestivalRepository festivalRepository) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.festivalRepository = festivalRepository;
    }

    @GetMapping("/index")
    public String mostrarIndex() {
        return "empleado/index";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Principal principal, Model model) {
        // Obtener festival activo
        Festival festival = festivalRepository.findAll().stream()
                .filter(f -> f.getNombre() != null && "Epicentro Gourmet UNLa 2026".equalsIgnoreCase(f.getNombre()))
                .findFirst()
                .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));

        model.addAttribute("festival", festival);

        // Obtener datos del empleado logueado
        String email = principal != null ? principal.getName() : "empleado@epicentro.com";

        // Buscar staff por email del usuario logueado o tomar el primero disponible
        Staff staff = staffRepository.findByUsuario_Email(email)
                .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElse(null));

        Map<String, Object> empleadoMap = new HashMap<>();
        if (staff != null) {
            long antiguedad = staff.getFechaIngreso() != null ?
                    ChronoUnit.YEARS.between(staff.getFechaIngreso(), LocalDate.now()) : 0;
            boolean esCajero = staff instanceof Cajero;
            String rol = esCajero ? "Cajero" : (staff instanceof Cocinero c && c.getCategoriaCocina() != null ? c.getCategoriaCocina().getNombre() : "Staff General");

            empleadoMap.put("id", staff.getId());
            empleadoMap.put("nombre", staff.getNombre() + " " + staff.getApellido());
            empleadoMap.put("dni", staff.getDni());
            empleadoMap.put("rol", rol);
            empleadoMap.put("antiguedad", antiguedad);
            empleadoMap.put("cajero", esCajero);
            empleadoMap.put("email", email);
        } else {
            // Datos ficticios de muestra para el panel del usuario
            empleadoMap.put("id", 1L);
            empleadoMap.put("nombre", "Martín González");
            empleadoMap.put("dni", "38452910");
            empleadoMap.put("rol", "Cajero");
            empleadoMap.put("antiguedad", 3L);
            empleadoMap.put("cajero", true);
            empleadoMap.put("email", email);
        }
        model.addAttribute("empleado", empleadoMap);

        // Asignación actual: buscar asignación real o proveer ficticia representativa
        UnidadVenta unidad = null;
        if (staff != null) {
            unidad = unidadVentaRepository.findAll().stream()
                    .filter(u -> (u.getResponsable() != null && u.getResponsable().getId() == staff.getId())
                            || (u.getStaffAsignado() != null && u.getStaffAsignado().stream().anyMatch(s -> s.getId() == staff.getId())))
                    .findFirst()
                    .orElseGet(() -> unidadVentaRepository.findAll().stream().findFirst().orElse(null));
        } else {
            unidad = unidadVentaRepository.findAll().stream().findFirst().orElse(null);
        }

        Map<String, Object> asignacion = new HashMap<>();
        asignacion.put("unidad", unidad != null ? unidad.getNombreComercial() : "Food Truck 1 - El Asador Viajero");
        asignacion.put("festival", festival != null ? festival.getNombre() : "Epicentro Gourmet UNLa 2026");
        model.addAttribute("asignacionActual", asignacion);

        return "reportes/empleadoDashboard";
    }
}