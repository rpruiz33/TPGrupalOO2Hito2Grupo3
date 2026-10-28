package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
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
    private final FestivalRepository festivalRepository;

    public PlatoResponsableController(
            PlatoRepository platoRepository,
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            FestivalRepository festivalRepository) {
        this.platoRepository = platoRepository;
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.festivalRepository = festivalRepository;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(
            Authentication authentication,
            Model model) {

        model.addAttribute("plato", new Plato());

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute(
                    "error",
                    "No se encontró el registro del Responsable asociado a tu usuario.");
            return "responsable/platos/nuevo";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/nuevo";
        }

        model.addAttribute("unidadVenta", unidad);

        List<Festival> festivales = festivalRepository
                .buscarFestivalActivoPorUnidadVenta(
                        unidad.getId(),
                        LocalDate.now());

        if (festivales.size() == 1) {
            model.addAttribute("festival", festivales.get(0));
        } else if (festivales.isEmpty()) {
            model.addAttribute(
                    "avisoFestival",
                    "Actualmente no hay un festival vigente para tu unidad de venta.");
        } else {
            model.addAttribute(
                    "avisoFestival",
                    "Hay más de un festival vigente para tu unidad de venta. Contactá al administrador.");
        }

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
            cargarDatosFormulario(authentication, model);
            return "responsable/platos/nuevo";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute(
                    "error",
                    "No se encontró el registro del Responsable asociado a tu usuario.");
            cargarDatosFormulario(authentication, model);
            return "responsable/platos/nuevo";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/nuevo";
        }

        // Validar que el precio de venta sea mayor que el costo de producción.
        if (plato.getPrecio() != null
                && plato.getCostoProduccion() != null
                && plato.getPrecio().compareTo(plato.getCostoProduccion()) <= 0) {

            model.addAttribute(
                    "error",
                    "El precio de venta debe ser mayor que el costo de producción.");

            cargarDatosFormulario(authentication, model);
            return "responsable/platos/nuevo";
        }

        plato.setId(null);
        plato.setUnidadVenta(unidad);

        platoRepository.save(plato);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "El plato se guardó correctamente.");

        return "redirect:/responsable/platos/nuevo";
    }

    private void cargarDatosFormulario(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            return;
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            return;
        }

        model.addAttribute("unidadVenta", unidad);

        List<Festival> festivales = festivalRepository
                .buscarFestivalActivoPorUnidadVenta(
                        unidad.getId(),
                        LocalDate.now());

        if (festivales.size() == 1) {
            model.addAttribute("festival", festivales.get(0));
        } else if (festivales.isEmpty()) {
            model.addAttribute(
                    "avisoFestival",
                    "Actualmente no hay un festival vigente para tu unidad de venta.");
        } else {
            model.addAttribute(
                    "avisoFestival",
                    "Hay más de un festival vigente para tu unidad de venta.");
        }
    }

@GetMapping
public String listarPlatos(
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
        return "responsable/platos/lista";
    }

    UnidadVenta unidad = unidadVentaRepository
            .findByResponsable_Id(responsable.getId())
            .orElse(null);

    if (unidad == null) {
        model.addAttribute(
                "error",
                "No tenés una unidad de venta asignada como Responsable.");
        return "responsable/platos/lista";
    }

    model.addAttribute("unidadVenta", unidad);

    model.addAttribute(
            "platos",
            platoRepository.findByUnidadVenta_IdOrderByNombreAsc(unidad.getId()));

    return "responsable/platos/lista";
}
    
}