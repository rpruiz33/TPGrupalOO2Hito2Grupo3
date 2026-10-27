
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.RolRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IUsuarioService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/responsable/usuarios")
public class AdminUsuarioController {

    private final IUsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioController(
            IUsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Listar usuarios
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "responsable/vistausuarios";
    }

    // alta
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setRol(new Rol());

        model.addAttribute("usuario", nuevoUsuario);
        model.addAttribute("roles", rolRepository.findAll());

        return "responsable/usuario-form";
    }

    // guardar 
    @PostMapping("/guardar")
    public String guardarUsuario(
            @ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttrs) {

        try {
            usuarioService.crearUsuario(usuario);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Usuario creado exitosamente. Se ha enviado un correo con sus credenciales de acceso."
            );

        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "El correo electrónico ya está registrado."
            );

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "Ocurrió un error al guardar el usuario."
            );
        }

        return "redirect:/responsable/usuarios";
    }

   
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttrs) {

        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/responsable/usuarios";
        }

        usuario.setPasswordHash("");

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());

        return "responsable/usuario-form";
    }

    
    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(
            @PathVariable("id") Long id,
            @ModelAttribute Usuario usuarioActualizado,
            RedirectAttributes redirectAttrs) {

        Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null);

        if (usuarioExistente == null) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/responsable/usuarios";
        }

        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setRol(usuarioActualizado.getRol());

        if (usuarioActualizado.getPasswordHash() != null
                && !usuarioActualizado.getPasswordHash().isEmpty()) {

            usuarioExistente.setPasswordHash(
                    passwordEncoder.encode(usuarioActualizado.getPasswordHash())
            );
        }

        usuarioRepository.save(usuarioExistente);

        redirectAttrs.addFlashAttribute(
                "mensaje",
                "Usuario actualizado exitosamente."
        );

        return "redirect:/responsable/usuarios";
    }

    
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        if (!usuarioRepository.existsById(id)) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/responsable/usuarios";
        }

        usuarioRepository.deleteById(id);

        redirectAttrs.addFlashAttribute(
                "mensaje",
                "Usuario eliminado exitosamente."
        );

        return "redirect:/responsable/usuarios";
    }

  
    @GetMapping("/reenviar/{id}")
    public String reenviarCredenciales(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {

        try {
            usuarioService.reenviarCredenciales(id);

            redirectAttrs.addFlashAttribute(
                    "mensaje",
                    "Se regeneró la contraseña y se enviaron las credenciales por correo."
            );

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(
                    "error",
                    "No se pudieron reenviar las credenciales."
            );
        }

        return "redirect:/responsable/usuarios";
    }
}
