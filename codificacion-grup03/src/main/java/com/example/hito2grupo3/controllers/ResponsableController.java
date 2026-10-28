package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/responsable")
public class ResponsableController {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public ResponsableController(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @GetMapping("/index")
    public String mostrarIndex(
            Authentication authentication,
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
                    "No se encontró el Responsable asociado a tu usuario.");

            return "responsable/index";
        }

        model.addAttribute(
                "nombreResponsable",
                responsable.getNombre() + " " + responsable.getApellido());

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        model.addAttribute("unidadVenta", unidad);

        return "responsable/index";
    }
}
