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
import org.springframework.web.bind.annotation.PathVariable;
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
        cargarDatosFormulario(authentication, model);

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

        if (result.hasErrors()) {
            cargarDatosFormulario(authentication, model);
            return "responsable/platos/nuevo";
        }

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
        plato.setActivo(true);

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

    @GetMapping("/modificar/{id}")
    public String mostrarFormularioModificar(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = obtenerUnidadResponsable(authentication);

        if (unidad == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tenés una unidad de venta asignada como Responsable.");
            return "redirect:/responsable/platos";
        }

        Plato plato = platoRepository.findById(id).orElse(null);

        if (plato == null
                || plato.getUnidadVenta() == null
                || !plato.getUnidadVenta().getId().equals(unidad.getId())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El plato no existe o no pertenece a tu unidad de venta.");
            return "redirect:/responsable/platos";
        }

        model.addAttribute("plato", plato);
        model.addAttribute("unidadVenta", unidad);

        return "responsable/platos/modificar";
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

        UnidadVenta unidad = obtenerUnidadResponsable(authentication);

        if (unidad == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tenés una unidad de venta asignada como Responsable.");
            return "redirect:/responsable/platos";
        }

        Plato plato = platoRepository.findById(id).orElse(null);

        if (plato == null
                || plato.getUnidadVenta() == null
                || !plato.getUnidadVenta().getId().equals(unidad.getId())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El plato no existe o no pertenece a tu unidad de venta.");
            return "redirect:/responsable/platos";
        }

        if (result.hasErrors()) {
            model.addAttribute("unidadVenta", unidad);
            return "responsable/platos/modificar";
        }

        if (datosFormulario.getPrecio() != null
                && datosFormulario.getCostoProduccion() != null
                && datosFormulario.getPrecio()
                        .compareTo(datosFormulario.getCostoProduccion()) <= 0) {

            model.addAttribute(
                    "error",
                    "El precio de venta debe ser mayor que el costo de producción.");
            model.addAttribute("unidadVenta", unidad);

            return "responsable/platos/modificar";
        }

        // Actualizamos solamente los campos editables.
        // El ID y la unidad de venta originales se conservan.
        plato.setNombre(datosFormulario.getNombre());
        plato.setPrecio(datosFormulario.getPrecio());
        plato.setCostoProduccion(datosFormulario.getCostoProduccion());
        plato.setActivo(datosFormulario.isActivo());

        platoRepository.save(plato);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "El plato se modificó correctamente.");

        return "redirect:/responsable/platos";
    }

    private UnidadVenta obtenerUnidadResponsable(
            Authentication authentication) {

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            return null;
        }

        return unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);
    }
}
