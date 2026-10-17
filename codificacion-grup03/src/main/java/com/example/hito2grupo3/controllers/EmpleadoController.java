package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public EmpleadoController(FestivalRepository festivalRepository,
                              UnidadVentaRepository unidadVentaRepository) {
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @GetMapping("/index")
    public String mostrarIndex() {
        return "empleado/index";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        Festival festival = festivalRepository.findAll().stream()
                .filter(f -> f.getNombre() != null && "Epicentro Gourmet UNLa 2026".equalsIgnoreCase(f.getNombre()))
                .findFirst()
                .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));

        model.addAttribute("festival", festival);

        List<UnidadVenta> lista = new ArrayList<>();
        if (festival != null && festival.getUnidadesVentaHabilitadas() != null && !festival.getUnidadesVentaHabilitadas().isEmpty()) {
            lista.addAll(festival.getUnidadesVentaHabilitadas());
        } else {
            lista.addAll(unidadVentaRepository.findAll());
        }
        lista.sort(Comparator.comparing(UnidadVenta::getCodigo, Comparator.nullsLast(String::compareTo)));

        model.addAttribute("unidades", lista);
        return "reportes/empleadoDashboard";
    }
}
