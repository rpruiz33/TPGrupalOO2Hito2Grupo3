package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.services.IEmpleadoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUsuarioController {

    private final IEmpleadoService empleadoService;

    public GlobalUsuarioController(IEmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @ModelAttribute("nombreResponsable")
    public String agregarNombreResponsable(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Responsable";
        }
        return empleadoService.obtenerNombreCompletoPorEmail(authentication.getName());
    }
}