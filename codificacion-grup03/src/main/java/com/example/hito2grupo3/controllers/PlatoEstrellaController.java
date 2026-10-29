package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IPedidoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/plato-estrella")
public class PlatoEstrellaController {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final FestivalRepository festivalRepository;
    private final IPedidoService pedidoService;

    public PlatoEstrellaController(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            FestivalRepository festivalRepository,
            IPedidoService pedidoService) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.festivalRepository = festivalRepository;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String mostrarFormulario(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute(
                    "error",
                    "No se encontró el responsable asociado al usuario.");
            return "responsable/platos/plato-estrella";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada.");
            return "responsable/platos/plato-estrella";
        }

        List<Festival> festivales = festivalRepository
                .buscarFestivalActivoPorUnidadVenta(
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

        Staff responsable = staffRepository
                .findByUsuario_Email(authentication.getName())
                .orElse(null);

        if (responsable == null) {
            model.addAttribute(
                    "error",
                    "No se encontró el responsable asociado al usuario.");
            return "responsable/platos/plato-estrella";
        }

        UnidadVenta unidad = unidadVentaRepository
                .findByResponsable_Id(responsable.getId())
                .orElse(null);

        if (unidad == null) {
            model.addAttribute(
                    "error",
                    "No tenés una unidad de venta asignada.");
            return "responsable/platos/plato-estrella";
        }

        List<Festival> festivales = festivalRepository
                .buscarFestivalActivoPorUnidadVenta(
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