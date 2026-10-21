package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.services.IReporteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    @Autowired
    private IReporteVentaService reporteVentaService;

    @GetMapping("/recaudacion/festival/{festivalId}")
    public String verReporteRecaudacion(@PathVariable Long festivalId, Model model) {
        try {
            ReporteVentaDTO reporte = reporteVentaService.obtenerTotalesFestival(festivalId);
            List<RankingUnidadDTO> ranking = reporteVentaService.obtenerRankingFestival(festivalId);
            
            model.addAttribute("reporte", reporte);
            model.addAttribute("ranking", ranking);
            
            return "reportes/ReporteRecaudacion"; 
        } catch (RuntimeException e) {
            return "redirect:/admin/dashboard?error=FestivalNoEncontrado";
        }
    }
}