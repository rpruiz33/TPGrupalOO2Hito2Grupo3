package com.example.hito2grupo3.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecaudacionController {
    @GetMapping("/recaudacion")
    public String finanzas() {
        return "reportes/recaudacion";
    }

    @GetMapping("/cierreCaja")
    public String cierreCaja() {
        return "reportes/cierreCaja";
    }
      @GetMapping( "/empleadoDashboard")
    public String  empleadoDashboard(){
        return "reportes/empleadoDashboard";
    }

      @GetMapping( "/reciboPdf")
    public String reciboPdf(){
        return "reportes/reciboPdf";
    }

    
    
}