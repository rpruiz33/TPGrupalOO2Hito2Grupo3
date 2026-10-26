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
import java.util.HashMap;
import java.util.Map;

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
    public String mostrarDashboard(Principal principal, Model model) {

        // Obtener el usuario autenticado.
        String email = principal != null ? principal.getName() : null;

        // Buscar exclusivamente al empleado asociado al usuario autenticado.
        Staff staff = email != null
                ? staffRepository.findByUsuario_Email(email).orElse(null)
                : null;

        // Cargar los datos del empleado.
        if (staff != null) {

            long antiguedad = staff.getFechaIngreso() != null
                    ? ChronoUnit.YEARS.between(
                            staff.getFechaIngreso(),
                            LocalDate.now())
                    : 0;

            boolean esCajero = staff instanceof Cajero;

            String rol = esCajero
                    ? "Cajero"
                    : (staff instanceof Cocinero cocinero
                            && cocinero.getCategoriaCocina() != null
                            ? cocinero.getCategoriaCocina().getNombre()
                            : "Staff General");

            Map<String, Object> empleado = new HashMap<>();

            empleado.put("id", staff.getId());
            empleado.put("nombre",
                    staff.getNombre() + " " + staff.getApellido());
            empleado.put("dni", staff.getDni());
            empleado.put("rol", rol);
            empleado.put("antiguedad", antiguedad);
            empleado.put("cajero", esCajero);
            empleado.put("email", email);

            model.addAttribute("empleado", empleado);

        } else {
            model.addAttribute("empleado", null);
        }

        // Buscar el festival activo para la fecha actual.
        LocalDate hoy = LocalDate.now();

        Festival festivalActivo = festivalRepository.findAll().stream()
                .filter(festival ->
                        festival.getFechaInicio() != null
                        && festival.getFechaFin() != null
                        && !hoy.isBefore(festival.getFechaInicio())
                        && !hoy.isAfter(festival.getFechaFin()))
                .findFirst()
                .orElse(null);

        // No asignar una unidad ni un festival ficticios.
        Map<String, Object> asignacionActual = null;

        if (staff != null && festivalActivo != null) {

            long staffId = staff.getId();
            long festivalId = festivalActivo.getId();

            UnidadVenta unidadAsignada = unidadVentaRepository.findAll()
                    .stream()
                    .filter(unidad -> {

                        boolean esResponsable =
                                unidad.getResponsable() != null
                                && unidad.getResponsable().getId() == staffId;

                        boolean estaEnStaffAsignado =
                                unidad.getStaffAsignado() != null
                                && unidad.getStaffAsignado().stream()
                                        .anyMatch(persona ->
                                                persona.getId() == staffId);

                        return esResponsable || estaEnStaffAsignado;
                    })
                    .filter(unidad ->
                            unidad.getFestivales() != null
                            && unidad.getFestivales().stream()
                                    .anyMatch(festival ->
                                            festival.getId() == festivalId))
                    .findFirst()
                    .orElse(null);

            if (unidadAsignada != null) {

                asignacionActual = new HashMap<>();

                asignacionActual.put(
                        "unidad",
                        unidadAsignada.getNombreComercial());

                asignacionActual.put(
                        "festival",
                        festivalActivo.getNombre());
            }
        }

        model.addAttribute("asignacionActual", asignacionActual);

        return "reportes/empleadoDashboard";
    }

    /**
     * Redirige al usuario autenticado al menú que le corresponde.
     */
   @GetMapping("/volver-al-menu")
public String volverAlMenu(
        Principal principal,
        org.springframework.security.core.Authentication authentication) {

    if (principal == null || authentication == null) {
        return "redirect:/login";
    }

    // Redirigir según los permisos reales del usuario.
    if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CAJERO"))) {
        return "redirect:/cajero/index";
    }

    if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"))) {
        return "redirect:/responsable/index";
    }

    if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"))) {
        return "redirect:/empleado/index";
    }

    // Si no tiene ninguno de los roles esperados, denegar el acceso.
    return "redirect:/access-denied";
}
}
