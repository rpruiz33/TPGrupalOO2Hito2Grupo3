package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.services.LiquidacionService;
import com.example.hito2grupo3.services.LiquidacionService.ResultadoLiquidacion;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionController(LiquidacionService liquidacionService,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.liquidacionService = liquidacionService;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @GetMapping("/liquidacion")
    public String liquidacion(@RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) Long unidadId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Double sueldo,
            Principal principal, Model model) {

        Festival festival = festivalRepository.findAll().stream()
                .filter(f -> f.getNombre() != null && "Epicentro Gourmet UNLa 2026".equalsIgnoreCase(f.getNombre()))
                .findFirst()
                .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));

        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("festival", festival);
        Long idFestival = festival != null ? festival.getId() : null;
        model.addAttribute("festivalId", idFestival);

        Double sueldoMostrar = sueldo;

        if (idFestival != null) {
            try {
                // principal.getName() es el email del usuario logueado
                ResultadoLiquidacion r = liquidacionService.liquidar(principal.getName(), festivalId);
                model.addAttribute("unidad", r.unidad());
                model.addAttribute("diasFestival", r.diasFestival());
                model.addAttribute("liquidaciones", r.liquidaciones());
                model.addAttribute("totalGeneral", r.totalGeneral());

                if (sueldoMostrar == null && r.liquidaciones() != null) {
                    sueldoMostrar = r.liquidaciones().stream()
                            .findFirst()
                            .map(l -> l.sueldoBase().doubleValue())
                            .orElse(null);
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        model.addAttribute("sueldo", sueldoMostrar);
        return "reportes/liquidacion";
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

                if (liquidacionDTO != null) {
                    Map<String, Object> reciboMap = new HashMap<>();
                    String[] partes = liquidacionDTO.nombreCompleto() != null
                            ? liquidacionDTO.nombreCompleto().split(" ", 2)
                            : new String[] { "", "" };
                    reciboMap.put("nombre", partes[0]);
                    reciboMap.put("apellido", partes.length > 1 ? partes[1] : "");
                    reciboMap.put("dni", liquidacionDTO.dni());
                    reciboMap.put("rol", liquidacionDTO.rol());
                    reciboMap.put("festival", f != null ? f.getNombre() : "Epicentro Gourmet UNLa 2026");
                    reciboMap.put("unidad", resultado.unidad());
                    reciboMap.put("diasFestival", resultado.diasFestival());
                    reciboMap.put("sueldoBase", liquidacionDTO.sueldoBase());
                    reciboMap.put("plusCategoria", liquidacionDTO.adicional());
                    reciboMap.put("montoAntiguedad", null);
                    reciboMap.put("aniosAntiguedad", liquidacionDTO.aniosAntiguedad());
                    reciboMap.put("coeficiente", liquidacionDTO.periodos() * 0.5);
                    reciboMap.put("total", liquidacionDTO.total());
                    reciboMap.put("fechaEmision", LocalDate.now());

                    model.addAttribute("recibo", reciboMap);
                }

                model.addAttribute("liquidacion", liquidacionDTO);
                model.addAttribute("unidad", resultado.unidad());
            } else {
                model.addAttribute("error", "No se encontró un festival activo para ver el recibo.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo obtener el recibo: " + e.getMessage());
        }

        return "reportes/reciboPdf";
    }
}