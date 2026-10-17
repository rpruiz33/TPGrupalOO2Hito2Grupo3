package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.impl.LiquidacionService;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','EMPLEADO')")
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
                              @RequestParam(required = false) Double sueldo,
                              Principal principal, Model model) {

        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("festivalId", festivalId);
        model.addAttribute("sueldo", sueldo);

        if (festivalId != null) {
            try {
                // principal.getName() es el email del usuario logueado
                var r = liquidacionService.liquidar(principal.getName(), festivalId);
                model.addAttribute("unidad", r.unidad());
                model.addAttribute("diasFestival", r.diasFestival());
                model.addAttribute("liquidaciones", r.liquidaciones());
                model.addAttribute("totalGeneral", r.totalGeneral());
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "reportes/liquidacion";   
    }


    @GetMapping({"/liquidacion/recibo/{id}", "/empleado/recibo/{id}/liquidaciones"})
    public String verReciboLiquidacion(@PathVariable Long id,
                                       @RequestParam(required = false) Long festivalId,
                                       Principal principal,
                                       Model model) {
        try {
            model.addAttribute("empleadoId", id);
            model.addAttribute("festivalId", festivalId);

            if (festivalId != null) {
                var resultado = liquidacionService.liquidar(principal.getName(), festivalId);
                
                var liquidacionDTO = resultado.liquidaciones().stream()
                        .filter(l -> l.empleadoId().equals(id))
                        .findFirst()
                        .orElse(null);

                model.addAttribute("liquidacion", liquidacionDTO);
                model.addAttribute("unidad", resultado.unidad());
                model.addAttribute("diasFestival", resultado.diasFestival());
                
                festivalRepository.findById(festivalId).ifPresent(f -> model.addAttribute("festival", f));
            } else {
                model.addAttribute("error", "Debe seleccionar un festival para ver el recibo.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo obtener el recibo: " + e.getMessage());
        }

        return "reportes/reciboPdf";
    }
}