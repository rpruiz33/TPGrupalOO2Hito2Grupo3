
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.PlatoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/responsable/platos")
public class PlatoResponsableController {

    private final PlatoRepository platoRepository;
    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public PlatoResponsableController(
            PlatoRepository platoRepository,
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.platoRepository = platoRepository;
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("plato", new Plato());
        return "responsable/platos/nuevo";
    }

    @PostMapping("/guardar")
    public String guardarPlato(
            @Valid @ModelAttribute("plato") Plato plato,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "responsable/platos/nuevo";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute("error",
                    "No se encontró el registro del Responsable asociado a tu usuario.");
            return "responsable/platos/nuevo";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute("error",
                    "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/nuevo";
        }

        plato.setId(null);
        plato.setUnidadVenta(unidad);

        platoRepository.save(plato);

        redirectAttributes.addFlashAttribute(
                "mensaje", "El plato se guardó correctamente.");

        return "redirect:/responsable/platos/nuevo";
    }
}
