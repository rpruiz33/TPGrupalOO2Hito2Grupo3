package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final FestivalRepository festivalRepository;

    public EmpleadoController(
            StaffRepository staffRepository,
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
    public String mostrarDashboard(
            Principal principal,
            Authentication authentication,
            Model model) {

        // Verificar que exista una sesión autenticada.
        if (principal == null || authentication == null) {
            return "redirect:/login";
        }

        String email = principal.getName();

        // Buscar al empleado vinculado al usuario autenticado.
        Staff staff = staffRepository.findByUsuario_Email(email)
                .orElse(null);

        if (staff == null) {
            model.addAttribute(
                    "error",
                    "No se encontró un empleado asociado al usuario "
                            + email
                            + ". Contactá con el administrador."
            );

            return "reportes/empleadoDashboard";
        }

        // Determinar el rol según los permisos de Spring Security.
        boolean esCajero = tieneRol(authentication, "ROLE_CAJERO");
        boolean esResponsable = tieneRol(authentication, "ROLE_RESPONSABLE");
        boolean esCocinero = tieneRol(authentication, "ROLE_COCINERO");

        // Evitar mostrar información de cajero a otro tipo de usuario.
        if (esCajero && !(staff instanceof Cajero)) {
            model.addAttribute(
                    "error",
                    "La cuenta tiene el rol de cajero, pero el empleado "
                            + "asociado no está registrado como Cajero. "
                            + "Contactá con el administrador."
            );

            return "reportes/empleadoDashboard";
        }

        // Calcular la antigüedad en años, meses y días.
        String antiguedad;

        if (staff.getFechaIngreso() != null) {
            LocalDate fechaIngreso = staff.getFechaIngreso();
            LocalDate hoy = LocalDate.now();

            if (fechaIngreso.isAfter(hoy)) {
                antiguedad = "Fecha de ingreso inválida";
            } else {
                Period periodo = Period.between(fechaIngreso, hoy);

                int anios = periodo.getYears();
                int meses = periodo.getMonths();
                int dias = periodo.getDays();

                if (anios > 0) {
                    antiguedad = anios
                            + (anios == 1 ? " año" : " años");

                    if (meses > 0) {
                        antiguedad += " y " + meses
                                + (meses == 1 ? " mes" : " meses");
                    }

                } else if (meses > 0) {
                    antiguedad = meses
                            + (meses == 1 ? " mes" : " meses");

                    if (dias > 0) {
                        antiguedad += " y " + dias
                                + (dias == 1 ? " día" : " días");
                    }

                } else {
                    antiguedad = dias
                            + (dias == 1 ? " día" : " días");
                }
            }
        } else {
            antiguedad = "Sin fecha registrada";
        }

        // Obtener el nombre del rol.
        String rol;

        if (esCajero) {
            rol = "Cajero";
        } else if (esResponsable) {
            rol = "Responsable";
        } else if (esCocinero
                && staff instanceof Cocinero cocinero
                && cocinero.getCategoriaCocina() != null) {

            rol = cocinero.getCategoriaCocina().getNombre();

        } else if (staff.getUsuario() != null
                && staff.getUsuario().getRol() != null) {

            rol = staff.getUsuario().getRol().getNombre();

        } else {
            rol = "Staff General";
        }

        // Preparar los datos personales y laborales.
        Map<String, Object> empleadoMap = new HashMap<>();

        empleadoMap.put("id", staff.getId());
        empleadoMap.put(
                "nombre",
                staff.getNombre() + " " + staff.getApellido()
        );
        empleadoMap.put("dni", staff.getDni());
        empleadoMap.put("rol", rol);
        empleadoMap.put("antiguedad", antiguedad);
        empleadoMap.put("cajero", esCajero);
        empleadoMap.put("email", email);

        // Incluir turno y caja exclusivamente para cajeros.
        if (esCajero) {
            Cajero cajero = (Cajero) staff;

            empleadoMap.put(
                    "turno",
                    cajero.getTurno() != null
                            ? cajero.getTurno()
                            : "No asignado"
            );

            empleadoMap.put(
                    "numeroCaja",
                    cajero.getNumeroCaja() != null
                            ? cajero.getNumeroCaja()
                            : "No asignada"
            );
        }

        model.addAttribute("empleado", empleadoMap);

        // Buscar el festival que está vigente hoy.
        LocalDate hoy = LocalDate.now();

        Festival festivalActivo = festivalRepository.findAll()
                .stream()
                .filter(f ->
                        f.getFechaInicio() != null
                                && f.getFechaFin() != null
                                && !hoy.isBefore(f.getFechaInicio())
                                && !hoy.isAfter(f.getFechaFin())
                )
                .findFirst()
                .orElse(null);

        model.addAttribute("festival", festivalActivo);

        // Buscar una unidad vinculada al empleado.
        UnidadVenta unidad = unidadVentaRepository.findAll()
                .stream()
                .filter(u ->
                        (u.getResponsable() != null
                                && Objects.equals(
                                        u.getResponsable().getId(),
                                        staff.getId()
                                ))
                        ||
                        (u.getStaffAsignado() != null
                                && u.getStaffAsignado()
                                        .stream()
                                        .anyMatch(s ->
                                                Objects.equals(
                                                        s.getId(),
                                                        staff.getId()
                                                )
                                        )
                                )
                )
                .findFirst()
                .orElse(null);

        // Mostrar la asignación solamente si existe un festival vigente
        // y una unidad asociada al empleado.
        if (unidad != null && festivalActivo != null) {
            Map<String, Object> asignacion = new HashMap<>();

            asignacion.put("unidad", unidad.getNombreComercial());
            asignacion.put("festival", festivalActivo.getNombre());

            model.addAttribute("asignacionActual", asignacion);
        }

        return "reportes/empleadoDashboard";
    }

    /**
     * Comprueba si el usuario autenticado posee un rol determinado.
     */
    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(rol)
                );
    }

    /**
     * Redirige al usuario autenticado al menú que le corresponde.
     */
    @GetMapping("/volver-al-menu")
    public String volverAlMenu(
            Principal principal,
            Authentication authentication) {

        if (principal == null || authentication == null) {
            return "redirect:/login";
        }

        if (tieneRol(authentication, "ROLE_CAJERO")) {
            return "redirect:/cajero/index";
        }

        if (tieneRol(authentication, "ROLE_RESPONSABLE")) {
            return "redirect:/responsable/index";
        }

        if (tieneRol(authentication, "ROLE_EMPLEADO")) {
            return "redirect:/empleado/index";
        }

        if (tieneRol(authentication, "ROLE_COCINERO")) {
            return "redirect:/cocinero/index";
        }

        if (tieneRol(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/index";
        }

        return "redirect:/access-denied";
    }
}
