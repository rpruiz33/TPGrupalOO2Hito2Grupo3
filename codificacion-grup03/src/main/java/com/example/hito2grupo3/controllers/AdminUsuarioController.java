
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IUsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping({"/responsable/usuarios", "/admin/usuarios"})
public class AdminUsuarioController {

    private final IUsuarioService usuarioService;
    private final IEmpleadoService empleadoService;
    private final UsuarioRepository usuarioRepository;
    private final StaffRepository staffRepository;
    private final CategoriaCocinaRepository categoriaCocinaRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public AdminUsuarioController(
            IUsuarioService usuarioService,
            IEmpleadoService empleadoService,
            UsuarioRepository usuarioRepository,
            StaffRepository staffRepository,
            CategoriaCocinaRepository categoriaCocinaRepository,
            UnidadVentaRepository unidadVentaRepository) {

        this.usuarioService = usuarioService;
        this.empleadoService = empleadoService;
        this.usuarioRepository = usuarioRepository;
        this.staffRepository = staffRepository;
        this.categoriaCocinaRepository = categoriaCocinaRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    // Listar empleados asignados a la unidad del responsable
    @GetMapping
    public String listarUsuarios(
            Authentication authentication,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String dni,
            Model model) {

        nombre = limpiarFiltro(nombre);
        apellido = limpiarFiltro(apellido);
        dni = limpiarFiltro(dni);

        if (authentication != null && authentication.isAuthenticated()) {
            Staff responsable = staffRepository
                    .findByUsuario_Email(authentication.getName())
                    .orElse(null);

            if (responsable != null) {
                UnidadVenta unidad = unidadVentaRepository
                        .findByResponsable_Id(responsable.getId())
                        .orElse(null);

                if (unidad != null) {
                    List<Staff> empleados = staffRepository.filtrarPorUnidad(
                            unidad.getId(),
                            nombre,
                            apellido,
                            dni
                    );
                    model.addAttribute("empleados", empleados);
                    model.addAttribute("unidadVenta", unidad);
                    model.addAttribute("nombre", nombre);
                    model.addAttribute("apellido", apellido);
                    model.addAttribute("dni", dni);
                    return "responsable/vistaempleados";
                }
            }
        }

        // Fallback para administradores o usuarios sin unidad de venta asociada
        List<Staff> empleados = staffRepository.findAll();
        model.addAttribute("empleados", empleados);
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("dni", dni);
        return "responsable/vistaempleados";
    }

    private String limpiarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    // Alta: muestra el formulario completo de empleado con envío automático de credenciales
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        EmpleadoDTO nuevoEmpleado = new EmpleadoDTO();
        nuevoEmpleado.setTipo("GENERAL");

        model.addAttribute("empleado", nuevoEmpleado);
        model.addAttribute("categoriasCocina", categoriaCocinaRepository.findAll());
        return "responsable/usuario-form";
    }

    // Guardar nuevo empleado/usuario
    @PostMapping("/guardar")
    public String guardarEmpleado(
            Authentication authentication,
            @ModelAttribute("empleado") EmpleadoDTO dto,
            RedirectAttributes redirectAttrs) {

        try {
            Staff nuevoStaff = empleadoService.crearEmpleado(dto);

            // Si el creador es responsable, asociar automáticamente el empleado a su unidad de venta
            if (authentication != null && authentication.isAuthenticated() && nuevoStaff != null) {
                Staff resp = staffRepository.findByUsuario_Email(authentication.getName()).orElse(null);
                if (resp != null) {
                    UnidadVenta uv = unidadVentaRepository.findByResponsable_Id(resp.getId()).orElse(null);
                    if (uv != null) {
                        uv.getStaffAsignado().add(nuevoStaff);
                        unidadVentaRepository.save(uv);
                    }
                }
            }

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
            return "responsable/usuario-form";

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

    // Dar de baja lógica — el {id} es el staff.id (viene del template)
    @GetMapping("/baja/{id}")
    public String darDeBaja(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.darDeBaja(id);
            redirectAttrs.addFlashAttribute("mensaje", "Empleado dado de baja. Su cuenta de acceso fue deshabilitada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al procesar la baja: " + e.getMessage());
        }

        return "redirect:/responsable/usuarios";
    }

    // Dar de alta (reactivar) empleado — el {id} es el staff.id (viene del template)
    @GetMapping("/reactivar/{id}")
    public String reactivar(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            empleadoService.reactivar(id);
            redirectAttrs.addFlashAttribute("mensaje",
                    "Empleado dado de alta exitosamente. Se regeneró la contraseña y se envió por correo.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al dar de alta el empleado: " + e.getMessage());
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
