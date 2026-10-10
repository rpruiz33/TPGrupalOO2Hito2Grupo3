package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String panelAdministrador(Model model) {
        // Acá luego podés inyectar repositorios para ver estadísticas, usuarios, etc.
        return "admin/dashboard"; 
    }
}