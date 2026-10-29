
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IUsuarioService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping({"/responsable/usuarios", "/admin/usuarios"})
public class AdminUsuarioController {

    private final IUsuarioService usuarioService;
    private final IEmpleadoService empleadoService;
    private final UsuarioRepository usuarioRepository;
    private final StaffRepository staffRepository;
    private final CategoriaCocinaRepository categoriaCocinaRepository;

    public AdminUsuarioController(
            IUsuarioService usuarioService,
            IEmpleadoService empleadoService,
            UsuarioRepository usuarioRepository,
            StaffRepository staffRepository,
            CategoriaCocinaRepository categoriaCocinaRepository) {

        this.usuarioService = usuarioService;
        this.empleadoService = empleadoService;
        this.usuarioRepository = usuarioRepository;
        this.staffRepository = staffRepository;
        this.categoriaCocinaRepository = categoriaCocinaRepository;
    }

    // Listar usuarios
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "responsable/vistausuarios";
    }

    // Alta: muestra el formulario completo de empleado con envío automático de credenciales
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        EmpleadoDTO nuevoEmpleado = new EmpleadoDTO();
        nuevoEmpleado.setTipo("GENERAL");

        model.addAttribute("empleado", nuevoEmpleado);
        model.addAttribute("categoriasCocina", categoriaCocinaRepository.findAll());
        return "admin/empleado-form";
    }

    // Guardar nuevo empleado/usuario
    @PostMapping("/guardar")
    public String guardarEmpleado(
            @ModelAttribute("empleado") EmpleadoDTO dto,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.crearEmpleado(dto);
            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Empleado registrado con éxito. Se ha enviado un correo con sus credenciales de acceso iniciales."
            );
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/responsable/usuarios/nuevo";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado al registrar el empleado: " + e.getMessage());
            return "redirect:/responsable/usuarios/nuevo";
        }

        return "redirect:/responsable/usuarios";
    }

    // Edición: muestra empleado-form.html con TODOS los campos de la entidad
    @GetMapping("/editar/{id}")
    @Transactional
    public String mostrarFormularioEdicion(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttrs) {

        try {
            // Resolver staff a partir de usuarioId o staffId
            Staff staff = null;
            Usuario usuario = usuarioRepository.findById(id).orElse(null);

            if (usuario != null) {
                staff = staffRepository.findByUsuario_Id(id)
                        .or(() -> staffRepository.findByUsuario_Email(usuario.getEmail()))
                        .orElse(null);

                if (staff == null) {
                    staff = new Staff();
                    staff.setNombre("");
                    staff.setApellido("");
                    staff.setDni("DNI-" + id);
                    staff.setFechaNacimiento(LocalDate.of(1995, 1, 1));
                    staff.setFechaIngreso(LocalDate.now());
                    staff.setSueldo(BigDecimal.ZERO);
                    staff.setActivo(usuario.isActivo());
                    staff.setUsuario(usuario);
                    staff = staffRepository.save(staff);
                }
            } else {
                staff = staffRepository.findById(id).orElse(null);
            }

            if (staff == null) {
                redirectAttrs.addFlashAttribute("error", "No se encontró el usuario o empleado con ID: " + id);
                return "redirect:/responsable/usuarios";
            }

            EmpleadoDTO dto = empleadoService.obtenerDtoPorId(staff.getId());
            model.addAttribute("empleado", dto);
            model.addAttribute("categoriasCocina", categoriaCocinaRepository.findAll());
            return "admin/empleado-form";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", "No se pudo cargar el empleado: " + e.getMessage());
            return "redirect:/responsable/usuarios";
        }
    }

    // Actualizar datos del empleado
    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado(
            @PathVariable("id") Long id,
            @ModelAttribute("empleado") EmpleadoDTO dto,
            RedirectAttributes redirectAttrs) {

        try {
            Staff staff = staffRepository.findByUsuario_Id(id)
                    .or(() -> staffRepository.findById(id))
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el empleado con ID: " + id));

            empleadoService.actualizarEmpleado(staff.getId(), dto);
            redirectAttrs.addFlashAttribute("mensaje", "Datos del empleado actualizados exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/responsable/usuarios/editar/" + id;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado al actualizar el empleado: " + e.getMessage());
            return "redirect:/responsable/usuarios/editar/" + id;
        }

        return "redirect:/responsable/usuarios";
    }

    // Eliminar: desvincula o da de baja el staff y elimina el usuario
    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminarUsuario(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            Staff staff = staffRepository.findById(id).orElse(null);
            if (staff != null) {
                empleadoService.darDeBaja(staff.getId());
                redirectAttrs.addFlashAttribute("mensaje", "Empleado dado de baja exitosamente.");
                return "redirect:/responsable/usuarios";
            }
            redirectAttrs.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/responsable/usuarios";
        }

        try {
            Optional<Staff> staffOpt = staffRepository.findByUsuario_Id(id)
                    .or(() -> staffRepository.findByUsuario_Email(usuario.getEmail()));

            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                staff.setUsuario(null);
                staff.setActivo(false);
                staffRepository.save(staff);
            }

            usuarioRepository.delete(usuario);
            redirectAttrs.addFlashAttribute("mensaje", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            redirectAttrs.addFlashAttribute("mensaje", "El usuario fue dado de baja exitosamente.");
        }

        return "redirect:/responsable/usuarios";
    }

    // Dar de baja lógica
    @GetMapping("/baja/{id}")
    @Transactional
    public String darDeBaja(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            Staff staff = staffRepository.findByUsuario_Id(id)
                    .or(() -> staffRepository.findById(id))
                    .orElse(null);

            if (staff != null) {
                empleadoService.darDeBaja(staff.getId());
            } else {
                usuarioRepository.findById(id).ifPresent(u -> {
                    u.setActivo(false);
                    usuarioRepository.save(u);
                });
            }
            redirectAttrs.addFlashAttribute("mensaje", "Empleado dado de baja lógicamente con éxito. Su usuario ha sido deshabilitado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al procesar la baja: " + e.getMessage());
        }

        return "redirect:/responsable/usuarios";
    }

    // Reactivar empleado
    @GetMapping("/reactivar/{id}")
    @Transactional
    public String reactivar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            Staff staff = staffRepository.findByUsuario_Id(id)
                    .or(() -> staffRepository.findById(id))
                    .orElse(null);

            if (staff != null) {
                empleadoService.reactivar(staff.getId());
            } else {
                usuarioRepository.findById(id).ifPresent(u -> {
                    u.setActivo(true);
                    usuarioRepository.save(u);
                    usuarioService.reenviarCredenciales(u.getId());
                });
            }
            redirectAttrs.addFlashAttribute("mensaje",
                    "Empleado reactivado exitosamente. Se ha regenerado su contraseña y enviado por correo electrónico.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al reactivar el empleado: " + e.getMessage());
        }

        return "redirect:/responsable/usuarios";
    }

    // Reenviar credenciales
    @GetMapping("/reenviar/{id}")
    public String reenviarCredenciales(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            Long usuarioId = id;
            if (!usuarioRepository.existsById(id)) {
                Staff s = staffRepository.findById(id).orElse(null);
                if (s != null && s.getUsuario() != null) {
                    usuarioId = s.getUsuario().getId();
                }
            }
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
