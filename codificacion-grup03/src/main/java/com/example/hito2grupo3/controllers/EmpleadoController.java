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

        // Obtener festival activo (el primero que no haya terminado, o el primero disponible)
        Festival festival = festivalRepository.findAll().stream()
                .filter(f -> f.getFechaFin() != null && !f.getFechaFin().isBefore(LocalDate.now()))
                .findFirst()
                .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));

        model.addAttribute("festival", festival);

        String email = principal != null ? principal.getName() : null;

        if (email == null) {
            model.addAttribute("error", "No se pudo identificar al usuario logueado.");
            return "reportes/empleadoDashboard";
        }

        // Buscar staff vinculado al usuario logueado por email
        Staff staff = staffRepository.findByUsuario_Email(email).orElse(null);

        if (staff == null) {
            model.addAttribute("error",
                    "No se encontró un empleado asociado al usuario \"" + email + "\". "
                    + "Contactá con el administrador.");
            return "reportes/empleadoDashboard";
        }

        // Calcular antigüedad real
        long antiguedad = staff.getFechaIngreso() != null
                ? ChronoUnit.YEARS.between(staff.getFechaIngreso(), LocalDate.now())
                : 0;

        // Determinar rol real
        boolean esCajero = staff instanceof Cajero;
        String rol;
        if (esCajero) {
            rol = "Cajero";
        } else if (staff instanceof Cocinero c && c.getCategoriaCocina() != null) {
            rol = c.getCategoriaCocina().getNombre();
        } else {
            rol = "Staff General";
        }

        // Armar el mapa de datos del empleado (sin nada hardcodeado)
        Map<String, Object> empleadoMap = new HashMap<>();
        empleadoMap.put("id", staff.getId());
        empleadoMap.put("nombre", staff.getNombre() + " " + staff.getApellido());
        empleadoMap.put("dni", staff.getDni());
        empleadoMap.put("rol", rol);
        empleadoMap.put("antiguedad", antiguedad);
        empleadoMap.put("cajero", esCajero);
        empleadoMap.put("email", email);

        if (esCajero) {
            Cajero cajero = (Cajero) staff;
            empleadoMap.put("turno", cajero.getTurno() != null ? cajero.getTurno() : "N/D");
            empleadoMap.put("numeroCaja", cajero.getNumeroCaja() != null ? cajero.getNumeroCaja() : "N/D");
        }

        model.addAttribute("empleado", empleadoMap);

        // Buscar unidad de venta real asignada al empleado
        UnidadVenta unidad = unidadVentaRepository.findAll().stream()
                .filter(u -> (u.getResponsable() != null && u.getResponsable().getId() == staff.getId())
                        || (u.getStaffAsignado() != null && u.getStaffAsignado().stream()
                                .anyMatch(s -> s.getId() == staff.getId())))
                .findFirst()
                .orElse(null);

        if (unidad != null) {
            Map<String, Object> asignacion = new HashMap<>();
            asignacion.put("unidad", unidad.getNombreComercial());
            asignacion.put("festival", festival != null ? festival.getNombre() : "Sin festival activo");
            model.addAttribute("asignacionActual", asignacion);
        }
        // Si unidad == null, asignacionActual no se pone en el model → la vista ya maneja ese caso

        return "reportes/empleadoDashboard";
    }
}