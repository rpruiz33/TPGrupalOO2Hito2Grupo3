package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.RolRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioController(UsuarioRepository usuarioRepository, 
                                  RolRepository rolRepository, 
                                  PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/VistaUsuarios"; 
    }

    
  @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setRol(new Rol()); 

        model.addAttribute("usuario", nuevoUsuario);
        model.addAttribute("roles", rolRepository.findAll()); 
        return "admin/usuario-form"; 
    }

    
    // 3. GUARDAR EL ALTA (Con manejo de errores)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttrs) {
        try {
            
            if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "La contraseña es obligatoria para un usuario nuevo.");
                return "redirect:/admin/usuarios";
            }

            
            usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
            
            
            usuarioRepository.save(usuario);
            redirectAttrs.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            
            redirectAttrs.addFlashAttribute("error", "Error: El correo electrónico ya está registrado.");
        } catch (Exception e) {
           
            redirectAttrs.addFlashAttribute("error", "Ocurrió un error inesperado al guardar el usuario.");
        }
        
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            redirectAttrs.addFlashAttribute("error", "El usuario no existe");
            return "redirect:/admin/usuarios";
        }
        
        usuario.setPasswordHash(""); 
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/Usuario-form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable("id") Long id, @ModelAttribute Usuario usuarioActualizado, RedirectAttributes redirectAttrs) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));

        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setRol(usuarioActualizado.getRol());

        if (usuarioActualizado.getPasswordHash() != null && !usuarioActualizado.getPasswordHash().isEmpty()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(usuarioActualizado.getPasswordHash()));
        }

        usuarioRepository.save(usuarioExistente);
        redirectAttrs.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
        return "redirect:/admin/usuarios";
    }

    
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        usuarioRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
        return "redirect:/admin/usuarios";
    }
}