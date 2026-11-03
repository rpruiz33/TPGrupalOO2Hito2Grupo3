package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccesoDenegadoController {

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403"; // Carga src/main/resources/templates/error/403.html
    }

}