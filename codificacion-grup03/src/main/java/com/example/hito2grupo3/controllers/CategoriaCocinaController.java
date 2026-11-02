package com.example.hito2grupo3.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.services.ICategoriaCocinaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categorias-cocina")
public class CategoriaCocinaController {

    private static final String VISTA_LISTA = "admin/categorias-cocina/lista";
    private static final String VISTA_FORM = "admin/categorias-cocina/form";

    private final ICategoriaCocinaService categoriaCocinaService;

    public CategoriaCocinaController(ICategoriaCocinaService categoriaCocinaService) {
        this.categoriaCocinaService = categoriaCocinaService;
    }

    // 1. Listado de Categorías de Cocina
    @GetMapping
    public String listarCategorias(Model model) {
        List<CategoriaCocinaDTO> categorias = categoriaCocinaService.listarTodasDTO();
        model.addAttribute("categorias", categorias);
        if (!model.containsAttribute("categoria")) {
            model.addAttribute("categoria", new CategoriaCocinaDTO());
        }
        return VISTA_LISTA;
    }

    // 2. Formulario para Nueva Categoría (soporta /nueva, /nuevo y /crear)
    @GetMapping({"/nueva", "/nuevo", "/crear"})
    public String mostrarFormularioNueva(Model model) {
        if (!model.containsAttribute("categoria")) {
            model.addAttribute("categoria", new CategoriaCocinaDTO());
        }
        model.addAttribute("esEdicion", false);
        return VISTA_FORM;
    }

    // 3. Procesar Alta de Categoría (soporta /guardar y /crear)
    @PostMapping({"/guardar", "/crear"})
    public String guardarCategoria(
            @Valid @ModelAttribute("categoria") CategoriaCocinaDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return VISTA_FORM;
        }

        try {
            categoriaCocinaService.crear(dto);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría '" + dto.getNombre() + "' registrada exitosamente.");
            return "redirect:/admin/categorias-cocina";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("esEdicion", false);
            return VISTA_FORM;
        } catch (Exception ex) {
            model.addAttribute("error", "Ocurrió un error inesperado al registrar la categoría.");
            model.addAttribute("esEdicion", false);
            return VISTA_FORM;
        }
    }

    // 4. Formulario para Editar Categoría Existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            CategoriaCocinaDTO dto = categoriaCocinaService.buscarDtoPorId(id);
            model.addAttribute("categoria", dto);
            model.addAttribute("esEdicion", true);
            return VISTA_FORM;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la categoría solicitada: " + ex.getMessage());
            return "redirect:/admin/categorias-cocina";
        }
    }

    // 5. Procesar Actualización de Categoría
    @PostMapping("/editar/{id}")
    public String actualizarCategoria(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoria") CategoriaCocinaDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            dto.setId(id);
            model.addAttribute("esEdicion", true);
            return VISTA_FORM;
        }

        try {
            categoriaCocinaService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría '" + dto.getNombre() + "' actualizada correctamente.");
            return "redirect:/admin/categorias-cocina";
        } catch (IllegalArgumentException ex) {
            dto.setId(id);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("esEdicion", true);
            return VISTA_FORM;
        } catch (Exception ex) {
            dto.setId(id);
            model.addAttribute("error", "Ocurrió un error al actualizar la categoría.");
            model.addAttribute("esEdicion", true);
            return VISTA_FORM;
        }
    }

    // 6. Baja Lógica (soporta GET y POST para máxima compatibilidad)
    @RequestMapping({"/eliminar/{id}", "/baja/{id}"})
    public String darDeBajaCategoria(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaCocinaService.darDeBaja(id);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría dada de baja lógicamente con éxito.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo dar de baja la categoría: " + ex.getMessage());
        }

        return "redirect:/admin/categorias-cocina";
    }

    // 7. Reactivar Categoría (soporta GET y POST)
    @RequestMapping("/reactivar/{id}")
    public String reactivarCategoria(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaCocinaService.reactivar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría reactivada con éxito.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo reactivar la categoría: " + ex.getMessage());
        }

        return "redirect:/admin/categorias-cocina";
    }

    // 8. Vista para Asignar Cocineros a la Categoría
    @GetMapping("/{id}/asignar")
    public String mostrarAsignacionCocineros(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            CategoriaCocinaDTO categoria = categoriaCocinaService.buscarDtoPorId(id);
            List<com.example.hito2grupo3.entities.Cocinero> asignados =
                    categoriaCocinaService.listarCocinerosPorCategoria(id);
            List<com.example.hito2grupo3.entities.Cocinero> todos =
                    categoriaCocinaService.listarTodosLosCocineros();

            model.addAttribute("categoria", categoria);
            model.addAttribute("cocinerosAsignados", asignados);
            model.addAttribute("todosCocineros", todos);

            return "admin/categorias-cocina/asignar";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la categoría: " + ex.getMessage());
            return "redirect:/admin/categorias-cocina";
        }
    }

    // 9. Procesar Asignación de Cocinero
    @PostMapping("/{id}/asignar")
    public String asignarCocinero(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam Long cocineroId,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaCocinaService.asignarCocineroACategoria(id, cocineroId);
            redirectAttributes.addFlashAttribute("mensaje", "Cocinero asignado a la categoría exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo asignar el cocinero: " + ex.getMessage());
        }

        return "redirect:/admin/categorias-cocina/" + id + "/asignar";
    }

    // 10. Procesar Desasignación de Cocinero
    @RequestMapping("/{id}/desasignar/{cocineroId}")
    public String desasignarCocinero(
            @PathVariable Long id,
            @PathVariable Long cocineroId,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaCocinaService.desasignarCocineroDeCategoria(cocineroId);
            redirectAttributes.addFlashAttribute("mensaje", "Cocinero desasignado de la categoría.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo desasignar el cocinero: " + ex.getMessage());
        }

        return "redirect:/admin/categorias-cocina/" + id + "/asignar";
    }
}
