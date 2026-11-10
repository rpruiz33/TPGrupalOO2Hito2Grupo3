package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.IRegistroUnidadVentaService;
import com.example.hito2grupo3.services.IUsuarioService;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.format.DateTimeParseException;

@Controller
public class UserController {

    private final IUsuarioService usuarioService;
    private final IFestivalService festivalService;
    private final IRegistroUnidadVentaService registroUnidadVentaService;

    public UserController(
            IUsuarioService usuarioService,
            IFestivalService festivalService,
            IRegistroUnidadVentaService registroUnidadVentaService) {

        this.usuarioService = usuarioService;
        this.festivalService = festivalService;
        this.registroUnidadVentaService = registroUnidadVentaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("festivales", festivalService.listarTodos());
        return "user/inicio";
    }

    @GetMapping("/login")
    public String login(
            Authentication authentication,
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout) {

        // Si ya hay una sesión autenticada válida, redirigir a su panel correspondiente
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (esAdmin) {
                return "redirect:/admin/dashboard";
            }

            boolean esResponsable = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));
            if (esResponsable) {
                return "redirect:/responsable/index";
            }

            boolean esCajero = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CAJERO"));
            if (esCajero) {
                return "redirect:/cajero/index";
            }

            return "redirect:/empleado/index";
        }

        model.addAttribute("error", error);
        model.addAttribute("logout", logout);

        return "user/login";
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambioPassword(
            Model model,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        model.addAttribute("email", email);
        model.addAttribute("esPrimerIngreso", usuarioService.debeCambiarPassword(email));

        return "user/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String procesarCambioPassword(
            @RequestParam("passwordActual") String passwordActual,
            @RequestParam("nuevaPassword") String nuevaPassword,
            @RequestParam("confirmarPassword") String confirmarPassword,
            Authentication authentication,
            RedirectAttributes redirectAttrs) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();

        try {
            usuarioService.cambiarPassword(
                    email,
                    passwordActual,
                    nuevaPassword,
                    confirmarPassword);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "¡Contraseña actualizada exitosamente!");

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean esResponsable = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));

            boolean esCajero = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CAJERO"));

            boolean esCocinero = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_COCINERO"));

            boolean esEmpleado = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));

            if (esAdmin) {
                return "redirect:/admin/dashboard";
            }

            if (esResponsable) {
                return "redirect:/responsable/index";
            }

            if (esCajero) {
                return "redirect:/cajero/index";
            }

            if (esCocinero || esEmpleado) {
                return "redirect:/empleado/index";
            }

            return "redirect:/acceso-denegado";

        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/cambiar-password";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ocurrió un error inesperado al cambiar la contraseña.");
            return "redirect:/cambiar-password";
        }
    }

    @GetMapping("/registro")
    public String registro() {
        return "user/registroUnidadVenta";
    }

    @PostMapping("/registro")
public String registrarUnidadVenta(
        @RequestParam(required = false) String nombreComercial,
        @RequestParam(required = false) String superficieM2,
        @RequestParam(required = false) String tipoUnidad,
        @RequestParam(required = false) String patente,
        @RequestParam(required = false) Boolean requiereElectricidad,
        @RequestParam(required = false) String cantidadCarpas,
        @RequestParam(required = false) String tiempoMontajeMin,
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) String apellido,
        @RequestParam(required = false) String dni,
        @RequestParam(required = false) String fechaNacimiento,
        @RequestParam(required = false) String fechaIngreso,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String password,
        Model model,
        RedirectAttributes redirectAttributes) {

    try {
        registroUnidadVentaService.registrar(
                nombreComercial,
                convertirDecimal(superficieM2, "La superficie"),
                tipoUnidad,
                patente,
                requiereElectricidad,
                convertirEntero(cantidadCarpas, "La cantidad de carpas"),
                convertirEntero(tiempoMontajeMin, "El tiempo de montaje"),
                nombre,
                apellido,
                dni,
                convertirFecha(fechaNacimiento, "La fecha de nacimiento"),
                convertirFecha(fechaIngreso, "La fecha de ingreso"),
                email,
                password);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "¡Registro exitoso! Ya podés iniciar sesión.");

        return "redirect:/login";

    } catch (IllegalArgumentException e) {

        // Mostrar el error sin perder los datos ingresados.
        model.addAttribute("error", e.getMessage());

        model.addAttribute("nombreComercial", nombreComercial);
        model.addAttribute("superficieM2", superficieM2);
        model.addAttribute("tipoUnidad", tipoUnidad);
        model.addAttribute("patente", patente);
        model.addAttribute("requiereElectricidad", requiereElectricidad);
        model.addAttribute("cantidadCarpas", cantidadCarpas);
        model.addAttribute("tiempoMontajeMin", tiempoMontajeMin);

        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("dni", dni);
        model.addAttribute("fechaNacimiento", fechaNacimiento);
        model.addAttribute("fechaIngreso", fechaIngreso);
        model.addAttribute("email", email);

        return "user/registroUnidadVenta";
    }
}
private BigDecimal convertirDecimal(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
        return null;
    }

    try {
        return new BigDecimal(valor.trim());
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                campo + " debe ser un número válido.");
    }
}

private Integer convertirEntero(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
        return null;
    }

    try {
        return Integer.valueOf(valor.trim());
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                campo + " debe ser un número entero válido.");
    }
}

private LocalDate convertirFecha(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
        return null;
    }

    try {
        return LocalDate.parse(valor.trim());
    } catch (DateTimeParseException e) {
        throw new IllegalArgumentException(
                campo + " no tiene un formato válido.");
    }
}
}