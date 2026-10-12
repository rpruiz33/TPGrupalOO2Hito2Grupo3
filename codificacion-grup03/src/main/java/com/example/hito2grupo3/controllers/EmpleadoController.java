
package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    @GetMapping("/index")
    public String mostrarIndex() {
        return "empleado/index";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard() {
        return "reportes/empleadoDashboard";
    }
}
