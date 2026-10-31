package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IFestivalService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/festivales")
public class AdminFestivalController {

    private final IFestivalService festivalService;
    private final UnidadVentaRepository unidadVentaRepository;

    public AdminFestivalController(IFestivalService festivalService,
                                   UnidadVentaRepository unidadVentaRepository) {
        this.festivalService = festivalService;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    // Listado de festivales
    @GetMapping
    public String listarFestivales(Model model) {
        model.addAttribute("festivales", festivalService.listarTodosDTO());
        return "admin/festivales";
    }

    // Formulario de alta de festival
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        FestivalDTO dto = new FestivalDTO();
        model.addAttribute("festival", dto);
        model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
        return "admin/festival-form";
    }

    // Guardar nuevo festival
    @PostMapping("/guardar")
    public String guardarFestival(@Valid @ModelAttribute("festival") FestivalDTO festivalDTO,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
            return "admin/festival-form";
        }

        try {
            festivalService.crearFestival(festivalDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Festival registrado exitosamente.");
            return "redirect:/admin/festivales";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
            return "admin/festival-form";
        }
    }

    // Formulario de edición de festival
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            FestivalDTO dto = festivalService.obtenerDtoPorId(id);
            model.addAttribute("festival", dto);
            model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
            return "admin/festival-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el festival solicitado: " + e.getMessage());
            return "redirect:/admin/festivales";
        }
    }

    // Actualizar festival existente
    @PostMapping("/editar/{id}")
    public String actualizarFestival(@PathVariable Long id,
                                     @Valid @ModelAttribute("festival") FestivalDTO festivalDTO,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            festivalDTO.setId(id);
            model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
            return "admin/festival-form";
        }

        try {
            festivalService.actualizarFestival(id, festivalDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Festival actualizado exitosamente.");
            return "redirect:/admin/festivales";
        } catch (IllegalArgumentException e) {
            festivalDTO.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("todasUnidades", unidadVentaRepository.findAll());
            return "admin/festival-form";
        }
    }

    // Baja lógica de festival
    @PostMapping("/baja/{id}")
    public String darDeBajaPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarBaja(id, redirectAttributes);
    }

    @GetMapping("/baja/{id}")
    public String darDeBajaGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarBaja(id, redirectAttributes);
    }

    private String procesarBaja(Long id, RedirectAttributes redirectAttributes) {
        try {
            festivalService.darDeBaja(id);
            redirectAttributes.addFlashAttribute("mensaje", "Festival dado de baja exitosamente (baja lógica).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al dar de baja el festival: " + e.getMessage());
        }
        return "redirect:/admin/festivales";
    }

    // Reactivación de festival
    @PostMapping("/reactivar/{id}")
    public String reactivarFestivalPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarReactivacion(id, redirectAttributes);
    }

    @GetMapping("/reactivar/{id}")
    public String reactivarFestivalGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarReactivacion(id, redirectAttributes);
    }

    private String procesarReactivacion(Long id, RedirectAttributes redirectAttributes) {
        try {
            festivalService.reactivar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Festival reactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al reactivar el festival: " + e.getMessage());
        }
        return "redirect:/admin/festivales";
    }

    // Eliminación (redirige a baja lógica para proteger integridad de datos)
    @PostMapping("/eliminar/{id}")
    public String eliminarFestivalPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarBaja(id, redirectAttributes);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarFestivalGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return procesarBaja(id, redirectAttributes);
    }

    // Detalle / Unidades de venta del festival
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String verUnidadesFestival(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Festival festival = festivalService.buscarPorId(id);
            model.addAttribute("festival", festival);
            model.addAttribute("unidades", festival.getUnidadesVentaHabilitadas());
            return "admin/unidades-festivales";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el festival indicado.");
            return "redirect:/admin/festivales";
        }
    }
}
