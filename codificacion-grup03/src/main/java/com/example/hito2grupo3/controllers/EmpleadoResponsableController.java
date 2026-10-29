package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/responsable/empleados")
public class EmpleadoResponsableController {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public EmpleadoResponsableController(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @GetMapping
    public String listarEmpleados(
            Authentication authentication,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String dni,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute(
                    "error",
                    "No se encontró el responsable asociado al usuario.");
            model.addAttribute("empleados", List.of());

            return "responsable/vistaempleados";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No se encontró una unidad de venta asociada al responsable.");
            model.addAttribute("empleados", List.of());

            return "responsable/vistaempleados";
        }

        nombre = limpiarFiltro(nombre);
        apellido = limpiarFiltro(apellido);
        dni = limpiarFiltro(dni);

        List<Staff> empleados = staffRepository.filtrarActivosPorUnidad(
                unidad.getId(),
                nombre,
                apellido,
                dni
        );

        model.addAttribute("empleados", empleados);
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("dni", dni);
        model.addAttribute("unidadVenta", unidad);

        return "responsable/vistaempleados";
    }

    private String limpiarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
