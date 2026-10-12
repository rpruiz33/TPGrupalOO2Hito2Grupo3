package com.example.hito2grupo3.controllers;

import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hito2grupo3.services.RentabilidadNetaService;

@Controller
public class RentabilidadNetaController {

    private final RentabilidadNetaService  r;
;

    public RentabilidadNetaController(RentabilidadNetaService rentabilidadNetaService  ){
        this.r = rentabilidadNetaService ;
    }

    // Req. 4: vista que renderiza selector + resumen + tabla de ranking.
    @GetMapping("/rentabilidadNeta")
    public String verRecaudacion(@RequestParam(required = false) Long festivalId, Model model) {
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("ranking", Collections.emptyList());
        model.addAttribute("esAdministrador", true);

        try {
            model.addAttribute("festivales", r.listarFestivales());
        } catch (Exception ex) {
            model.addAttribute("festivales", Collections.emptyList());
            model.addAttribute("error", "No se pudo cargar la lista de festivales. Revise la conexion a la base de datos.");
            return "reportes/recaudacion";
        }

        if (festivalId != null) {
            try {
                model.addAttribute("reporte", r.obtenerReporteVenta(festivalId));
                model.addAttribute("ranking", r.obtenerRankingUnidades(festivalId));
            } catch (Exception ex) {
                model.addAttribute("error", ex.getMessage() != null ? ex.getMessage()
                        : "No se pudo obtener el reporte. Revise la conexion a la base de datos.");
            }
        }

        return "reportes/rentabilidadNeta";
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
	public String reciboPdf () {
		return "reportes/reciboPdf";
	}
}