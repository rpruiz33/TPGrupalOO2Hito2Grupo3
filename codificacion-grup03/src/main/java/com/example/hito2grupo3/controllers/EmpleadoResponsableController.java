package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/responsable/empleados")
public class EmpleadoResponsableController {

    @GetMapping
    public String listarEmpleados() {
        return "redirect:/responsable/usuarios";
    }
}

