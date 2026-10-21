package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.IReporteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IReporteVentaService reporteVentaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        
        long cantFestivales = festivalRepository.count();
        long cantUsuarios = usuarioRepository.count();
        BigDecimal recaudacionTotal = festivalRepository.findAll().stream()
                .map(fest -> {
                    try {
                        ReporteVentaDTO dto = reporteVentaService.obtenerTotalesFestival(fest.getId());
                        return (dto != null && dto.getRecaudacionTotal() != null) 
                               ? dto.getRecaudacionTotal() 
                               : BigDecimal.ZERO;
                    } catch (Exception e) {
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        model.addAttribute("cantFestivalesActivos", cantFestivales);
        model.addAttribute("cantUsuariosRegistrados", cantUsuarios);
        model.addAttribute("recaudacionMensual", recaudacionTotal);

        return "admin/dashboard"; 
    }
}