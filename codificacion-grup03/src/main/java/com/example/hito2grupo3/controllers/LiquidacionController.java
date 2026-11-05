package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.services.ILiquidacionService;

import java.security.Principal;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','EMPLEADO','CAJERO','COCINERO')")
public class LiquidacionController {

    private final ILiquidacionService liquidacionService;

    public LiquidacionController(ILiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/liquidacion")
    public String liquidacion(@RequestParam(required = false) Long festivalId,
                              @RequestParam(required = false) Long unidadId,
                              @RequestParam(required = false) String codigo,
                              @RequestParam(required = false) Double sueldo,
                              Authentication authentication,
                              Principal principal,
                              Model model) {

        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esResponsable = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));
        boolean esEmpleado = !esAdmin && !esResponsable;

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esResponsable", esResponsable);
        model.addAttribute("esEmpleado", esEmpleado);

        Festival festival = liquidacionService.resolverFestivalPorDefecto(festivalId);
        model.addAttribute("festivales", liquidacionService.listarFestivales());
        model.addAttribute("festival", festival);

        Long idFestival = festival != null ? festival.getId() : null;
        model.addAttribute("festivalId", idFestival);

        if (esAdmin) {
            model.addAttribute("todasLasUnidades", liquidacionService.listarTodasLasUnidades());
        }

        if (idFestival != null && principal != null) {
            try {
                ILiquidacionService.ResultadoLiquidacion r = liquidacionService.liquidarSegunRol(
                        principal.getName(),
                        idFestival,
                        unidadId,
                        codigo,
                        esAdmin,
                        esResponsable
                );

                if (unidadId != null && unidadId > 0) {
                    model.addAttribute("unidadId", unidadId);
                }

                model.addAttribute("unidad", r.unidad());
                model.addAttribute("diasFestival", r.diasFestival());
                model.addAttribute("liquidaciones", r.liquidaciones());
                model.addAttribute("totalGeneral", r.totalGeneral());

                if (esEmpleado && r.liquidaciones() != null && !r.liquidaciones().isEmpty()) {
                    var miLiq = r.liquidaciones().iterator().next();
                    model.addAttribute("empleadoNombre", miLiq.nombreCompleto());
                    model.addAttribute("empleadoEmail", miLiq.email());
                }

            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "reportes/liquidacion";
    }

    @GetMapping({ "/reciboPdf/{id}", "/liquidacion/recibo/{id}", "/empleado/recibo/{id}/liquidaciones" })
    public String verReciboLiquidacion(@PathVariable Long id,
                                       @RequestParam(required = false) Long festivalId,
                                       @RequestParam(required = false) Long unidadId,
                                       Authentication authentication,
                                       Principal principal,
                                       Model model) {

        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esResponsable = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));
        boolean esEmpleado = !esAdmin && !esResponsable;

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esResponsable", esResponsable);
        model.addAttribute("esEmpleado", esEmpleado);

        try {
            String email = principal != null ? principal.getName() : null;
            Map<String, Object> reciboData = liquidacionService.obtenerModeloRecibo(
                    id,
                    festivalId,
                    unidadId,
                    email,
                    esAdmin,
                    esEmpleado
            );
            model.addAllAttributes(reciboData);
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo obtener el recibo: " + e.getMessage());
        }

        return "reportes/reciboPdf";
    }
}