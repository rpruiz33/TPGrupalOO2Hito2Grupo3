
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
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
import java.time.LocalDate;

@Controller
@RequestMapping("/responsable/usuarios")
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

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(
                authentication.getName()
        );

        List<Staff> empleados = List.of();

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada como Responsable."
            );
        } else {
            empleados = empleadoService.filtrarEmpleadosPorUnidad(
                unidad.getId(),
                nombre,
                apellido,
                dni,
                "TODOS",
                rolFiltro,
                email
                );
        }

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

    private String limpiarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {

        EmpleadoDTO nuevoEmpleado = new EmpleadoDTO();
        nuevoEmpleado.setTipo("GENERAL");
        nuevoEmpleado.setFechaIngreso(LocalDate.now());
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
LocalDate hoy = LocalDate.now();

if (dto.getFechaIngreso() != null
        && (dto.getFechaIngreso().isBefore(hoy)
        || dto.getFechaIngreso().isAfter(hoy.plusMonths(1)))) {

    result.rejectValue(
            "fechaIngreso",
            "fechaIngreso.invalida",
            "La fecha de ingreso debe ser desde hoy hasta un mes en adelante."
    );
}
        if (result.hasErrors()) {
            model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
            return "responsable/usuario-form";
        }

        try {
            Staff nuevoStaff = empleadoService.crearEmpleado(dto);

            if (authentication != null && authentication.isAuthenticated() && nuevoStaff != null) {
                empleadoService.asociarEmpleadoAUnidadDelCreador(
                        authentication.getName(),
                        nuevoStaff
                );
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
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttrs) {

    try {
        Staff staff = obtenerEmpleadoDeUnidad(id, authentication);

        if (staff == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "El empleado no existe o no pertenece a tu unidad de venta."
            );

            return "redirect:/responsable/usuarios";
        }

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
        Authentication authentication,
        @Valid @ModelAttribute("empleado") EmpleadoDTO dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttrs) {

    Staff staff = obtenerEmpleadoDeUnidad(id, authentication);

    if (staff == null) {
        redirectAttrs.addFlashAttribute(
                "error",
                "No podés modificar este empleado porque no pertenece a tu unidad de venta."
        );

        return "redirect:/responsable/usuarios";
    }

    if (result.hasErrors()) {
        dto.setId(staff.getId());
        model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
        return "responsable/usuario-form";
    }

    try {
        empleadoService.actualizarEmpleado(staff.getId(), dto);

        redirectAttrs.addFlashAttribute(
                "mensaje",
                "Datos del empleado actualizados exitosamente."
        );

    } catch (IllegalArgumentException e) {
        dto.setId(staff.getId());
        model.addAttribute("error", e.getMessage());
        model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
        return "responsable/usuario-form";

    } catch (Exception e) {
        dto.setId(staff.getId());
        model.addAttribute(
                "error",
                "Error inesperado al actualizar el empleado: " + e.getMessage()
        );
        model.addAttribute("categoriasCocina", categoriaCocinaService.listarTodas());
        return "responsable/usuario-form";
    }

    return "redirect:/responsable/usuarios";
}


/*METODO DECLARADO PERO NO SE UTILIZA, NO ESTA IMPLEMENTADO
    SOLO SE DAN BAJAS LOGICAS 
    @PostMapping("/eliminar/{id}")
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
            redirectAttrs.addFlashAttribute(
                    "error",
                    "Error al procesar la baja: " + e.getMessage()
            );
        }

        return "redirect:/responsable/usuarios";
    }
*/
@PostMapping("/baja/{id}")
public String darDeBaja(
        @PathVariable("id") Long id,
        Authentication authentication,
        RedirectAttributes redirectAttrs) {

    try {
        Staff staff = obtenerEmpleadoDeUnidad(id, authentication);

        if (staff == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No podés dar de baja a este empleado porque no pertenece a tu unidad de venta."
            );

            return "redirect:/responsable/usuarios";
        }

        empleadoService.darDeBaja(staff.getId());

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
@PostMapping("/reactivar/{id}")
public String reactivar(
        @PathVariable("id") Long id,
        Authentication authentication,
        RedirectAttributes redirectAttrs) {

    try {
        Staff staff = obtenerEmpleadoDeUnidad(id, authentication);

        if (staff == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No podés reactivar este empleado porque no pertenece a tu unidad de venta."
            );

            return "redirect:/responsable/usuarios";
        }

        empleadoService.reactivar(staff.getId());

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

@PostMapping("/reenviar/{id}")
public String reenviarCredenciales(
        @PathVariable("id") Long id,
        Authentication authentication,
        RedirectAttributes redirectAttrs) {

    try {
        Staff staff = obtenerEmpleadoDeUnidad(id, authentication);

        if (staff == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No podés reenviar credenciales de un empleado que no pertenece a tu unidad de venta."
            );

            return "redirect:/responsable/usuarios";
        }

        if (staff.getUsuario() == null) {
            throw new IllegalStateException(
                    "El empleado no tiene una cuenta de usuario vinculada."
            );
        }

        usuarioService.reenviarCredenciales(staff.getUsuario().getId());

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
    private Staff obtenerEmpleadoDeUnidad(
        Long id,
        Authentication authentication) {

    if (authentication == null || !authentication.isAuthenticated()) {
        return null;
    }

    UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(
            authentication.getName()
    );

    if (unidad == null) {
        return null;
    }

    Staff staff = empleadoService.buscarStaffExistenteDesdeId(id)
            .orElse(null);

    if (staff == null) {
        return null;
    }

        boolean perteneceALaUnidad = unidad.getStaffAsignado().stream()
        .anyMatch(asignado -> asignado.getId() == staff.getId());

    return perteneceALaUnidad ? staff : null;
}
}
