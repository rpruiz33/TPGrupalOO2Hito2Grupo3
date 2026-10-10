package com.example.hito2grupo3.controllers;

import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hito2grupo3.exceptions.ResourceNotFoundException;
import com.example.hito2grupo3.services.RecaudacionService;

@Controller
public class RecaudacionController {

    private final RecaudacionService recaudacionService;

    public RecaudacionController(RecaudacionService recaudacionService) {
        this.recaudacionService = recaudacionService;
    }

    // Req. 4: vista que renderiza selector + resumen + tabla de ranking.
    @GetMapping("/recaudacion")
    public String verRecaudacion(@RequestParam(required = false) Long festivalId, Model model) {
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("ranking", Collections.emptyList());

        // Sin login, habilitamos la visualizacion completa del reporte/ranking.
        model.addAttribute("esAdministrador", true);

        try {
            model.addAttribute("festivales", recaudacionService.listarFestivales());
        } catch (Exception ex) {
            model.addAttribute("festivales", Collections.emptyList());
            model.addAttribute("error", "No se pudo cargar la lista de festivales. Revise la conexion a la base de datos.");
            return "reportes/recaudacion";
        }

        if (festivalId != null) {
            try {
                model.addAttribute("reporte", recaudacionService.obtenerReporteVenta(festivalId));
                model.addAttribute("ranking", recaudacionService.obtenerRankingUnidades(festivalId));
            } catch (ResourceNotFoundException ex) {
                model.addAttribute("error", ex.getMessage());
            } catch (Exception ex) {
                model.addAttribute("error", "No se pudo obtener el reporte. Revise la conexion a la base de datos.");
            }
        }

        return "reportes/recaudacion";
    }

    @GetMapping("/cierreCaja")
	public String cierreCaja() {
		return "reportes/cierreCaja";
	}

    @GetMapping("/empleadoDashboard")
	public String empleadoDashboard() {
		return "reportes/empleadosDashboard";
	}

    @GetMapping("/reciboPdf")
	public String reciboPdf() {
		return "reportes/reciboPdf";
	}
}
