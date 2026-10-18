package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IUsuarioService;
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

    public UserController(IUsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout) {
        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        return "user/login";
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambioPassword(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        model.addAttribute("email", email);
        model.addAttribute("esPrimerIngreso", usuario != null && usuario.isDebeCambiarPassword());
        return "user/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String procesarCambioPassword(@RequestParam("passwordActual") String passwordActual,
                                         @RequestParam("nuevaPassword") String nuevaPassword,
                                         @RequestParam("confirmarPassword") String confirmarPassword,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttrs) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        try {
            usuarioService.cambiarPassword(email, passwordActual, nuevaPassword, confirmarPassword);
            redirectAttrs.addFlashAttribute("mensaje", "¡Contraseña actualizada exitosamente!");

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return esAdmin ? "redirect:/admin/dashboard" : "redirect:/empleado/index";
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/cambiar-password";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ocurrió un error inesperado al cambiar la contraseña.");
            return "redirect:/cambiar-password";
        }
    }
}
