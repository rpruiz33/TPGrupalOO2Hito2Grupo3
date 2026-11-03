package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.IPlatoService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/responsable/platos")
public class PlatoResponsableController {

    private final IPlatoService platoService;
    private final IFestivalService festivalService;

    public PlatoResponsableController(
            IPlatoService platoService,
            IFestivalService festivalService) {
        this.platoService = platoService;
        this.festivalService = festivalService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(
            Authentication authentication,
            Model model) {

        model.addAttribute("plato", new Plato());

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = platoService.obtenerUnidadDelResponsable(authentication.getName());
        if (unidad == null) {
            model.addAttribute("error", "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/nuevo";
        }

        model.addAttribute("unidadVenta", unidad);
        cargarDatosFestival(unidad.getId(), model);

        return "responsable/platos/nuevo";
    }

    @PostMapping("/guardar")
    public String guardarPlato(
            @Valid @ModelAttribute("plato") Plato plato,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = platoService.obtenerUnidadDelResponsable(authentication.getName());
        if (unidad == null) {
            model.addAttribute("error", "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/nuevo";
        }

        if (result.hasErrors()) {
            model.addAttribute("unidadVenta", unidad);
            cargarDatosFestival(unidad.getId(), model);
            return "responsable/platos/nuevo";
        }

        try {
            platoService.crearPlato(plato, unidad.getId());
            redirectAttributes.addFlashAttribute("mensaje", "El plato se guardó correctamente.");
            return "redirect:/responsable/platos/nuevo";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("unidadVenta", unidad);
            cargarDatosFestival(unidad.getId(), model);
            return "responsable/platos/nuevo";
        }
    }

    @GetMapping
    public String listarPlatos(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = platoService.obtenerUnidadDelResponsable(authentication.getName());
        if (unidad == null) {
            model.addAttribute("error", "No tenés una unidad de venta asignada como Responsable.");
            return "responsable/platos/lista";
        }

        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("platos", platoService.listarPorUnidad(unidad.getId()));

        return "responsable/platos/lista";
    }

    @GetMapping("/modificar/{id}")
    public String mostrarFormularioModificar(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = platoService.obtenerUnidadDelResponsable(authentication.getName());
        if (unidad == null) {
            redirectAttributes.addFlashAttribute("error", "No tenés una unidad de venta asignada como Responsable.");
            return "redirect:/responsable/platos";
        }

        try {
            Plato plato = platoService.buscarPorIdYUnidad(id, unidad.getId());
            model.addAttribute("plato", plato);
            model.addAttribute("unidadVenta", unidad);
            return "responsable/platos/modificar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/responsable/platos";
        }
    }

    @PostMapping("/modificar/{id}")
    public String ModificarPlato(
            @PathVariable Long id,
            @Valid @ModelAttribute("plato") Plato datosFormulario,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = platoService.obtenerUnidadDelResponsable(authentication.getName());
        if (unidad == null) {
            redirectAttributes.addFlashAttribute("error", "No tenés una unidad de venta asignada como Responsable.");
            return "redirect:/responsable/platos";
        }

        if (result.hasErrors()) {
            model.addAttribute("unidadVenta", unidad);
            return "responsable/platos/modificar";
        }

        try {
            platoService.actualizarPlato(id, datosFormulario, unidad.getId());
            redirectAttributes.addFlashAttribute("mensaje", "El plato se modificó correctamente.");
            return "redirect:/responsable/platos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("unidadVenta", unidad);
            return "responsable/platos/modificar";
        }
    }

    private void cargarDatosFestival(Long unidadVentaId, Model model) {
        List<Festival> festivales = festivalService.buscarFestivalActivoPorUnidadVenta(
                unidadVentaId,
                LocalDate.now());

        if (festivales.size() == 1) {
            model.addAttribute("festival", festivales.get(0));
        } else if (festivales.isEmpty()) {
            model.addAttribute("avisoFestival", "Actualmente no hay un festival vigente para tu unidad de venta.");
        } else {
            model.addAttribute("avisoFestival", "Hay más de un festival vigente para tu unidad de venta.");
        }
    }
}
