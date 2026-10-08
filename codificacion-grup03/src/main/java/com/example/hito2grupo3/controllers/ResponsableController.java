package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/responsable")
public class ResponsableController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "responsable/responsableDashboard";
    }

    @GetMapping("/empleados")
    public String empleados() {
        return "responsable/empleados/lista";
    }
}