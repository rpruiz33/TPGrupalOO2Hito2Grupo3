package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.LiquidacionService;
import com.example.hito2grupo3.services.LiquidacionService.ResultadoLiquidacion;
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
                              Principal principal, Model model) {

        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("festivalId", festivalId);

        if (festivalId != null) {
            try {
                // principal.getName() es el email del usuario logueado
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


    @GetMapping("/empleado/recibo/{id}/liquidaciones")
public String verReciboLiquidacion(@PathVariable Long id,
                                   @RequestParam(required = false) Long festivalId,
                                   Principal principal,
                                   Model model) {
    try {
        model.addAttribute("empleadoId", id);
        model.addAttribute("festivalId", festivalId);

        // Validar que se haya recibido un festivalId
        if (festivalId != null) {
            ResultadoLiquidacion resultado = liquidacionService.liquidar(principal.getName(), festivalId);
            
            // Buscar el DTO del empleado puntual en la lista calculada
            var liquidacionDTO = resultado.liquidaciones().stream()
                    .filter(l -> l.empleadoId().equals(id))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("liquidacion", liquidacionDTO);
            model.addAttribute("unidad", resultado.unidad());
        } else {
            model.addAttribute("error", "Debe seleccionar un festival para ver el recibo.");
        }
    } catch (Exception e) {
        // En lugar de arrojar Error 500, capturamos la excepción y la mostramos en la vista
        model.addAttribute("error", "No se pudo obtener el recibo: " + e.getMessage());
    }

    return "reportes/recibo"; // Asegurate de que exista src/main/resources/templates/reportes/recibo.html
}
}