package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.impl.LiquidacionServiceImpl;
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

    private final LiquidacionServiceImpl liquidacionService;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionController(LiquidacionServiceImpl liquidacionService,
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
                ILiquidacionService.ResultadoLiquidacion r;

                if (unidadId != null) {
                    r = liquidacionService.liquidarPorUnidad(unidadId, idFestival);
                    model.addAttribute("unidadId", unidadId);
                } else if (codigo != null && !codigo.isBlank()) {
                    UnidadVenta uv = unidadVentaRepository.findAll().stream()
                            .filter(u -> codigo.equalsIgnoreCase(u.getCodigo()))
                            .findFirst()
                            .orElse(null);
                    if (uv != null) {
                        r = liquidacionService.liquidarPorUnidad(uv.getId(), idFestival);
                        model.addAttribute("unidadId", uv.getId());
                    } else {
                        r = liquidacionService.liquidar(principal.getName(), idFestival);
                    }
                } else {
                    r = liquidacionService.liquidar(principal.getName(), idFestival);
                }

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

    @GetMapping({ "/reciboPdf/{id}", "/liquidacion/recibo/{id}", "/empleado/recibo/{id}/liquidaciones" })
    public String verReciboLiquidacion(@PathVariable Long id,
            @RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) Long unidadId,
            Principal principal,
            Model model) {
        try {
            Long targetFestivalId = festivalId;
            if (targetFestivalId == null) {
                Festival f = festivalRepository.findAll().stream()
                        .filter(fest -> fest.getNombre() != null
                                && "Epicentro Gourmet UNLa 2026".equalsIgnoreCase(fest.getNombre()))
                        .findFirst()
                        .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));
                if (f != null) {
                    targetFestivalId = f.getId();
                }
            }

            model.addAttribute("empleadoId", id);
            model.addAttribute("festivalId", targetFestivalId);
            model.addAttribute("unidadId", unidadId);

            if (targetFestivalId != null) {
                Festival f = festivalRepository.findById(targetFestivalId).orElse(null);
                ILiquidacionService.ResultadoLiquidacion resultado;
                if (unidadId != null) {
                    resultado = liquidacionService.liquidarPorUnidad(unidadId, targetFestivalId);
                } else {
                    resultado = liquidacionService.liquidar(principal.getName(), targetFestivalId);
                }

                var liquidacionDTO = resultado.liquidaciones().stream()
                        .filter(l -> Objects.equals(l.empleadoId(), id))
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