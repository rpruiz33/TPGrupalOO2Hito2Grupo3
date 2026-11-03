package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.IPedidoService;
import com.example.hito2grupo3.services.IUnidadVentaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/responsable/plato-estrella")
public class PlatoEstrellaController {

    private final IUnidadVentaService unidadVentaService;
    private final IFestivalService festivalService;
    private final IPedidoService pedidoService;

    public PlatoEstrellaController(
            IUnidadVentaService unidadVentaService,
            IFestivalService festivalService,
            IPedidoService pedidoService) {
        this.unidadVentaService = unidadVentaService;
        this.festivalService = festivalService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String mostrarFormulario(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada o no se encontró tu usuario como responsable.");
            return "responsable/platos/plato-estrella";
        }

        List<Festival> festivales = festivalService.buscarFestivalActivoPorUnidadVenta(
                unidad.getId(),
                LocalDate.now());

        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("festivales", festivales);

        if (festivales.isEmpty()) {
            model.addAttribute(
                    "aviso",
                    "No hay festivales vigentes para tu unidad de venta.");
        }

        return "responsable/platos/plato-estrella";
    }

    @PostMapping
    public String consultarPlatoEstrella(
            @RequestParam Long festivalId,
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UnidadVenta unidad = unidadVentaService.buscarPorResponsableEmail(authentication.getName());

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada o no se encontró tu usuario como responsable.");
            return "responsable/platos/plato-estrella";
        }

        List<Festival> festivales = festivalService.buscarFestivalActivoPorUnidadVenta(
                unidad.getId(),
                LocalDate.now());

        model.addAttribute("unidadVenta", unidad);
        model.addAttribute("festivales", festivales);

        boolean festivalValido = festivales.stream()
                .anyMatch(f -> f.getId().equals(festivalId));

        if (!festivalValido) {
            model.addAttribute(
                    "error",
                    "El festival seleccionado no está vigente para tu unidad de venta.");
            return "responsable/platos/plato-estrella";
        }

        PlatoDTO platoEstrella = pedidoService.obtenerPlatoEstrella(
                unidad.getId(),
                festivalId);

        if (platoEstrella == null) {
            model.addAttribute(
                    "aviso",
                    "Todavía no hay pedidos para este festival y unidad de venta.");
        } else {
            Festival festivalSeleccionado = festivales.stream()
                    .filter(f -> f.getId().equals(festivalId))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("platoEstrella", platoEstrella);
            model.addAttribute("festivalSeleccionado", festivalId);
            model.addAttribute(
                    "festivalSeleccionadoNombre",
                    festivalSeleccionado != null
                            ? festivalSeleccionado.getNombre()
                            : "Festival seleccionado");
        }

        return "responsable/platos/plato-estrella";
    }
}