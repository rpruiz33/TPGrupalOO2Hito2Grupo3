package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.LiquidacionService;
import com.example.hito2grupo3.services.LiquidacionService.ResultadoLiquidacion;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;
    private final FestivalRepository festivalRepository;

    public LiquidacionController(LiquidacionService liquidacionService,
                                 FestivalRepository festivalRepository) {
        this.liquidacionService = liquidacionService;
        this.festivalRepository = festivalRepository;
    }

    @GetMapping("/liquidacion")
    public String liquidacion(@RequestParam(required = false) Long festivalId,
                              Principal principal, Model model) {

        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("festivalId", festivalId);

        if (festivalId != null) {
            try {
                // principal.getName() es el email del responsable logueado
                ResultadoLiquidacion r = liquidacionService.liquidar(principal.getName(), festivalId);
                model.addAttribute("unidad", r.unidad());
                model.addAttribute("diasFestival", r.diasFestival());
                model.addAttribute("liquidaciones", r.liquidaciones());
                model.addAttribute("totalGeneral", r.totalGeneral());
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "reportes/liquidacion";   // templates/reportes/liquidacion.html
    }
}