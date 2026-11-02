package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.services.ICategoriaCocinaService;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import com.example.hito2grupo3.services.IUsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping({"/responsable/usuarios", "/admin/usuarios"})
public class AdminUsuarioController {

    private final IUsuarioService usuarioService;
    private final IEmpleadoService empleadoService;
    private final ICategoriaCocinaService categoriaCocinaService;
    private final IUnidadVentaService unidadVentaService;

    public AdminUsuarioController(
            IUsuarioService usuarioService,
            IEmpleadoService empleadoService,
            ICategoriaCocinaService categoriaCocinaService,
            IUnidadVentaService unidadVentaService) {

        this.usuarioService = usuarioService;
        this.empleadoService = empleadoService;
        this.categoriaCocinaService = categoriaCocinaService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping
    public String listarUsuarios(
            Authentication authentication,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "ACTIVOS") String estado,
            @RequestParam(required = false) String rol,
            Model model) {

        nombre = limpiarFiltro(nombre);
        apellido = limpiarFiltro(apellido);
        dni = limpiarFiltro(dni);
        email = limpiarFiltro(email);

        if (!List.of("ACTIVOS", "INACTIVOS", "TODOS").contains(estado)) {
            estado = "ACTIVOS";
        }

        String rolFiltro = limpiarFiltro(rol);
        if (rolFiltro != null) {
            rolFiltro = rolFiltro.toUpperCase(Locale.ROOT);
            if (!List.of("EMPLEADO", "CAJERO", "COCINERO").contains(rolFiltro)) {
                rolFiltro = null;
            }
        }

        if (authentication != null && authentication.isAuthenticated()) {
            UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());

            if (unidad != null) {
                List<Staff> empleados = empleadoService.filtrarEmpleadosPorUnidad(
                        unidad.getId(),
                        nombre,
                        apellido,
                        dni,
                        estado,
                        rolFiltro,
                        email
                );

                model.addAttribute("empleados", empleados);
                model.addAttribute("unidadVenta", unidad);
                model.addAttribute("nombre", nombre);
                model.addAttribute("apellido", apellido);
                model.addAttribute("dni", dni);
                model.addAttribute("email", email);
                model.addAttribute("estado", estado);
                model.addAttribute("rol", rolFiltro);

                return "responsable/vistaempleados";
            }
        }

        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);

        return "responsable/vistausuarios";
    }

    private String limpiarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        EmpleadoDTO nuevoEmpleado = new EmpleadoDTO();
        nuevoEmpleado.setTipo("GENERAL");

        model.addAttribute("empleado", nuevoEmpleado);
        model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());

        return "responsable/usuario-form";
    }

    @PostMapping("/guardar")
    public String guardarEmpleado(
            Authentication authentication,
            @Valid @ModelAttribute("empleado") EmpleadoDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";
        }

        try {
            Staff nuevoStaff = empleadoService.crearEmpleado(dto);

            if (authentication != null && authentication.isAuthenticated() && nuevoStaff != null) {
                empleadoService.asociarEmpleadoAUnidadDelCreador(authentication.getName(), nuevoStaff);
            }

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Empleado registrado con éxito. Se ha enviado un correo con sus credenciales de acceso iniciales."
            );

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";

        } catch (Exception e) {
            model.addAttribute(
                    "error",
                    "Error inesperado al registrar el empleado: " + e.getMessage()
            );
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";
        }

        return "redirect:/responsable/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttrs) {

        try {
            Staff staff = empleadoService.resolverStaffDesdeId(id);

            EmpleadoDTO dto = empleadoService.obtenerDtoPorId(staff.getId());

            model.addAttribute("empleado", dto);
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());

            return "responsable/usuario-form";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No se pudo cargar el empleado: " + e.getMessage()
            );

            return "redirect:/responsable/usuarios";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("empleado") EmpleadoDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";
        }

        try {
            Staff staff = empleadoService.resolverStaffDesdeId(id);
            empleadoService.actualizarEmpleado(staff.getId(), dto);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Datos del empleado actualizados exitosamente."
            );

        } catch (IllegalArgumentException e) {
            dto.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";

        } catch (Exception e) {
            dto.setId(id);
            model.addAttribute(
                    "error",
                    "Error inesperado al actualizar el empleado: " + e.getMessage()
            );
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";
        }

        return "redirect:/responsable/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.eliminarODarDeBajaPorId(id);
            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Usuario o empleado eliminado / dado de baja exitosamente."
            );
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al procesar la baja: " + e.getMessage());
        }

        return "redirect:/responsable/usuarios";
    }

    @GetMapping("/baja/{id}")
    public String darDeBaja(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.darDeBaja(id);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Empleado dado de baja. Su cuenta de acceso fue deshabilitada."
            );

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "Error al procesar la baja: " + e.getMessage()
            );
        }

        return "redirect:/responsable/usuarios";
    }

    @GetMapping("/reactivar/{id}")
    public String reactivar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.reactivar(id);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Empleado dado de alta exitosamente."
            );

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "Error al dar de alta el empleado: " + e.getMessage()
            );
        }

        return "redirect:/responsable/usuarios";
    }

    @GetMapping("/reenviar/{id}")
    public String reenviarCredenciales(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            Long usuarioId = empleadoService.resolverUsuarioId(id);
            usuarioService.reenviarCredenciales(usuarioId);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Se regeneró la contraseña y se enviaron las credenciales por correo exitosamente."
            );

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No se pudieron reenviar las credenciales: " + e.getMessage()
            );
        }

        return "redirect:/responsable/usuarios";
    }
}