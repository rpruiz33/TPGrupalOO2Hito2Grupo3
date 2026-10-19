
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IUsuarioService;
import com.example.hito2grupo3.services.RegistroUnidadVentaService;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final IUsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final RegistroUnidadVentaService registroUnidadVentaService;

    public UserController(
            IUsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            FestivalRepository festivalRepository,
            RegistroUnidadVentaService registroUnidadVentaService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.festivalRepository = festivalRepository;
        this.registroUnidadVentaService = registroUnidadVentaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute(
                "festivales",
                festivalRepository.findAllByOrderByFechaInicioAsc()
        );
        return "user/inicio";
    }

    @GetMapping("/login")
    public String login(
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout) {
        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        return "user/login";
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambioPassword(
            Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        model.addAttribute("email", email);
        model.addAttribute(
                "esPrimerIngreso",
                usuario != null && usuario.isDebeCambiarPassword()
        );
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
                    email, passwordActual, nuevaPassword, confirmarPassword
            );
            redirectAttrs.addFlashAttribute(
                    "mensaje", "¡Contraseña actualizada exitosamente!"
            );

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return esAdmin
                    ? "redirect:/admin/dashboard"
                    : "redirect:/empleado/index";
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/cambiar-password";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "Ocurrió un error inesperado al cambiar la contraseña."
            );
            return "redirect:/cambiar-password";
        }
    }

    @GetMapping("/registro")
    public String registro() {
        return "user/registroUnidadVenta";
    }

    @PostMapping("/registro")
    public String registrarUnidadVenta(
            @RequestParam String nombreComercial,
            @RequestParam BigDecimal superficieM2,
            @RequestParam String tipoUnidad,
            @RequestParam(required = false) String patente,
            @RequestParam(required = false) Boolean requiereElectricidad,
            @RequestParam(required = false) Integer cantidadCarpas,
            @RequestParam(required = false) Integer tiempoMontajeMin,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String dni,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimiento,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaIngreso,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        try {
            registroUnidadVentaService.registrar(
                    nombreComercial,
                    superficieM2,
                    tipoUnidad,
                    patente,
                    requiereElectricidad,
                    cantidadCarpas,
                    tiempoMontajeMin,
                    nombre,
                    apellido,
                    dni,
                    fechaNacimiento,
                    fechaIngreso,
                    email,
                    password
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "¡Registro exitoso! Ya podés iniciar sesión."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }

        return "redirect:/login";
    }
}
