package com.example.hito2grupo3.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FinanzasController {
    @GetMapping("/finanzas")
    public String finanzas() {
        return "reportes/finanzas";
    }
}