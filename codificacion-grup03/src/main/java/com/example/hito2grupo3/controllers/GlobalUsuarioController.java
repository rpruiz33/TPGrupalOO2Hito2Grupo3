package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.repositories.StaffRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUsuarioController {

    private final StaffRepository staffRepository;

    public GlobalUsuarioController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @ModelAttribute("nombreResponsable")
    public String agregarNombreResponsable(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Responsable";
        }

        String email = authentication.getName();

        // 1. Intento por email directo
        Staff staff = staffRepository.findByUsuario_Email(email).orElse(null);

        // 2. Si no lo encuentra, intento ignorando mayúsculas/minúsculas o espacios
        if (staff == null && email != null) {
            staff = staffRepository.findAll().stream()
                    .filter(s -> s.getUsuario() != null 
                              && s.getUsuario().getEmail() != null 
                              && s.getUsuario().getEmail().trim().equalsIgnoreCase(email.trim()))
                    .findFirst()
                    .orElse(null);
        }

        // Si encontró al Staff, devuelve su nombre y apellido
        if (staff != null && staff.getNombre() != null) {
            String apellido = staff.getApellido() != null ? " " + staff.getApellido() : "";
            return staff.getNombre() + apellido;
        }

        // Si no está registrado como Staff pero el usuario tiene nombre propio
        return "Responsable";
    }
}