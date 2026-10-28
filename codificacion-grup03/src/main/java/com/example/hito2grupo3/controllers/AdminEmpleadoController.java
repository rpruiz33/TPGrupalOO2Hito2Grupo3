package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;
import com.example.hito2grupo3.services.IEmpleadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/empleados")
public class AdminEmpleadoController {

    private final IEmpleadoService empleadoService;
    private final CategoriaCocinaRepository categoriaCocinaRepository;

    public AdminEmpleadoController(IEmpleadoService empleadoService,
                                   CategoriaCocinaRepository categoriaCocinaRepository) {
        this.empleadoService = empleadoService;
        this.categoriaCocinaRepository = categoriaCocinaRepository;
    }

    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.listarTodos());
        return "admin/VistaEmpleados";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        EmpleadoDTO nuevoEmpleado = new EmpleadoDTO();
        nuevoEmpleado.setTipo("GENERAL");

        model.addAttribute("empleado", nuevoEmpleado);
        model.addAttribute("categoriasCocina", categoriaCocinaRepository.findAll());
        return "admin/empleado-form";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(@ModelAttribute("empleado") EmpleadoDTO dto,
                                  RedirectAttributes redirectAttrs) {
        try {
            empleadoService.crearEmpleado(dto);
            redirectAttrs.addFlashAttribute("mensaje",
                    "Empleado registrado con éxito. Se ha enviado un correo con sus credenciales de acceso iniciales.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/empleados/nuevo";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado al registrar el empleado: " + e.getMessage());
            return "redirect:/admin/empleados/nuevo";
        }

        return "redirect:/admin/empleados";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id,
                                          Model model,
                                          RedirectAttributes redirectAttrs) {
        try {
            EmpleadoDTO dto = empleadoService.obtenerDtoPorId(id);
            model.addAttribute("empleado", dto);
            model.addAttribute("categoriasCocina", categoriaCocinaRepository.findAll());
            return "admin/empleado-form";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "No se pudo cargar el empleado: " + e.getMessage());
            return "redirect:/admin/empleados";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado(@PathVariable("id") Long id,
                                     @ModelAttribute("empleado") EmpleadoDTO dto,
                                     RedirectAttributes redirectAttrs) {
        try {
            empleadoService.actualizarEmpleado(id, dto);
            redirectAttrs.addFlashAttribute("mensaje", "Datos del empleado actualizados exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/empleados/editar/" + id;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado al actualizar el empleado: " + e.getMessage());
            return "redirect:/admin/empleados/editar/" + id;
        }

        return "redirect:/admin/empleados";
    }

    @GetMapping("/baja/{id}")
    public String darDeBajaEmpleado(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttrs) {
        try {
            empleadoService.darDeBaja(id);
            redirectAttrs.addFlashAttribute("mensaje", "Empleado dado de baja lógicamente con éxito. Su usuario ha sido deshabilitado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al procesar la baja: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @GetMapping("/reactivar/{id}")
    public String reactivarEmpleado(@PathVariable("id") Long id,
                                    RedirectAttributes redirectAttrs) {
        try {
            empleadoService.reactivar(id);
            redirectAttrs.addFlashAttribute("mensaje",
                    "Empleado reactivado exitosamente. Se ha regenerado su contraseña y enviado por correo electrónico para que la modifique al ingresar.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al reactivar el empleado: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @GetMapping("/reenviar/{id}")
    public String reenviarCredenciales(@PathVariable("id") Long id,
                                       RedirectAttributes redirectAttrs) {
        try {
            empleadoService.reenviarCredenciales(id);
            redirectAttrs.addFlashAttribute("mensaje",
                    "Se ha regenerado la contraseña y enviado el correo con las nuevas credenciales de acceso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al reenviar credenciales: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }
}
