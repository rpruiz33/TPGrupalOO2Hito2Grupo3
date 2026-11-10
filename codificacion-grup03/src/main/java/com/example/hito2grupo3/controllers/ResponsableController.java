package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.ICanonService;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IUnidadVentaService;
import com.example.hito2grupo3.services.ILiquidacionService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/responsable")
public class ResponsableController {

    private final IUnidadVentaService unidadVentaService;
    private final IEmpleadoService empleadoService;
    private final ICanonService canonService;
    private final ILiquidacionService liquidacionService;

    public ResponsableController(
            IUnidadVentaService unidadVentaService,
            IEmpleadoService empleadoService,
            ICanonService canonService,
            ILiquidacionService liquidacionService) {
        this.unidadVentaService = unidadVentaService;
        this.empleadoService = empleadoService;
        this.canonService = canonService;
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/index")
    public String mostrarIndex(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = empleadoService.buscarPorEmail(authentication.getName());
        if (responsable == null) {
            model.addAttribute("error", "No se encontró el Responsable asociado a tu usuario.");
            return "responsable/index";
        }

        model.addAttribute("nombreResponsable", responsable.getNombre() + " " + responsable.getApellido());
        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());
        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("festivalActual", obtenerFestivalActual(unidad));
        model.addAttribute("festivales", liquidacionService.listarFestivales());

        return "responsable/index";
    }

    @GetMapping("/canon")
    public String calcularCanon(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = empleadoService.buscarPorEmail(authentication.getName());
        if (responsable == null) {
            model.addAttribute("error", "No se encontró el Responsable asociado a tu usuario.");
            return "responsable/index";
        }

        model.addAttribute("nombreResponsable", responsable.getNombre() + " " + responsable.getApellido());
        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());
        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("festivalActual", obtenerFestivalActual(unidad));
        model.addAttribute("festivales", liquidacionService.listarFestivales());

        if (unidad == null) {
            model.addAttribute("error", "No tenés una unidad de venta asignada.");
        } else {
            model.addAttribute("canon", canonService.calcularCanon(unidad));
        }

        return "responsable/index";
    }

    private Festival obtenerFestivalActual(UnidadVenta unidad) {
        if (unidad == null || unidad.getFestivales() == null) {
            return null;
        }

        LocalDate hoy = LocalDate.now();
        return unidad.getFestivales().stream()
                .filter(festival -> festival.isActivo()
                        && festival.getFechaInicio() != null
                        && festival.getFechaFin() != null
                        && !hoy.isBefore(festival.getFechaInicio())
                        && !hoy.isAfter(festival.getFechaFin()))
                .findFirst()
                .orElse(null);
    }
}
