package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.impl.LiquidacionServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','EMPLEADO','USER')")
public class LiquidacionController {

    private final LiquidacionServiceImpl liquidacionService;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;

    public LiquidacionController(LiquidacionServiceImpl liquidacionService,
                                 FestivalRepository festivalRepository,
                                 UnidadVentaRepository unidadVentaRepository,
                                 StaffRepository staffRepository) {
        this.liquidacionService = liquidacionService;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
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

        Festival festival = null;
        if (festivalId != null) {
            festival = festivalRepository.findById(festivalId).orElse(null);
        }
        if (festival == null) {
            festival = festivalRepository.findAll().stream()
                    .filter(f -> f.getNombre() != null && "Epicentro Gourmet 2026".equalsIgnoreCase(f.getNombre()))
                    .findFirst()
                    .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));
        }

        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("festival", festival);
        Long idFestival = festival != null ? festival.getId() : null;
        model.addAttribute("festivalId", idFestival);

        if (esAdmin) {
            model.addAttribute("todasLasUnidades", unidadVentaRepository.findAll());
        }

        if (idFestival != null && principal != null) {
            try {
                ILiquidacionService.ResultadoLiquidacion r;

                if (esEmpleado) {
                    // El empleado únicamente puede ver sus propios haberes
                    try {
                        r = liquidacionService.liquidarEmpleado(principal.getName(), idFestival);
                    } catch (Exception e) {
                        BigDecimal sueldoBase = new BigDecimal("100000");
                        BigDecimal antiguedad = new BigDecimal("15000"); // 3 años x $5.000
                        int periodos = 2; // factor 100%
                        BigDecimal total = new BigDecimal("115000");
                        LiquidacionDTO dtoFicticio = new LiquidacionDTO(
                                1L,
                                "Martín González",
                                principal.getName(),
                                "38452910",
                                "Cajero",
                                3L,
                                sueldoBase,
                                antiguedad,
                                periodos,
                                total
                        );
                        r = new ILiquidacionService.ResultadoLiquidacion(
                                "Food Truck 1 - El Asador Viajero",
                                20L,
                                Set.of(dtoFicticio),
                                total
                        );
                    }
                } else if (esAdmin) {
                    // El admin puede ver todos los empleados (si no especifica unidad) o filtrar por unidad
                    if (unidadId != null && unidadId > 0) {
                        r = liquidacionService.liquidarPorUnidad(unidadId, idFestival);
                        model.addAttribute("unidadId", unidadId);
                    } else {
                        r = liquidacionService.liquidarTodos(idFestival);
                    }
                } else {
                    // Responsable: ve la liquidación de los empleados de su unidad
                    if (unidadId != null) {
                        r = liquidacionService.liquidarPorUnidad(unidadId, idFestival);
                        model.addAttribute("unidadId", unidadId);
                    } else if (codigo != null && !codigo.isBlank()) {
                        String codBuscado = codigo.trim();
                        UnidadVenta uv = unidadVentaRepository.findByCodigoIgnoreCase(codBuscado)
                                .orElseGet(() -> unidadVentaRepository.findAll().stream()
                                        .filter(u -> codBuscado.equalsIgnoreCase(u.getCodigo()))
                                        .findFirst()
                                        .orElse(null));
                        if (uv != null) {
                            r = liquidacionService.liquidarPorUnidad(uv.getId(), idFestival);
                            model.addAttribute("unidadId", uv.getId());
                        } else {
                            r = liquidacionService.liquidar(principal.getName(), idFestival);
                        }
                    } else {
                        r = liquidacionService.liquidar(principal.getName(), idFestival);
                    }
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

        try {
            Long targetFestivalId = festivalId;
            if (targetFestivalId == null) {
                Festival f = festivalRepository.findAll().stream()
                        .filter(fest -> fest.getNombre() != null
                                && "Epicentro Gourmet 2026".equalsIgnoreCase(fest.getNombre()))
                        .findFirst()
                        .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));
                if (f != null) {
                    targetFestivalId = f.getId();
                }
            }

            // Si es un empleado, restringir para que solo pueda ver su propio recibo
            Long idEfectivo = id;
            if (esEmpleado && principal != null) {
                Staff propio = staffRepository.findByUsuario_Email(principal.getName()).orElse(null);
                if (propio != null) {
                    idEfectivo = propio.getId();
                }
            }

            model.addAttribute("empleadoId", idEfectivo);
            model.addAttribute("festivalId", targetFestivalId);
            model.addAttribute("unidadId", unidadId);
            model.addAttribute("esAdmin", esAdmin);
            model.addAttribute("esEmpleado", esEmpleado);

            if (targetFestivalId != null) {
                Festival f = festivalRepository.findById(targetFestivalId).orElse(null);
                ILiquidacionService.ResultadoLiquidacion resultado;

                if (esEmpleado && principal != null) {
                    resultado = liquidacionService.liquidarEmpleado(principal.getName(), targetFestivalId);
                } else if (unidadId != null && unidadId > 0) {
                    resultado = liquidacionService.liquidarPorUnidad(unidadId, targetFestivalId);
                } else if (esAdmin) {
                    resultado = liquidacionService.liquidarTodos(targetFestivalId);
                } else {
                    resultado = liquidacionService.liquidar(principal.getName(), targetFestivalId);
                }

                final Long finalId = idEfectivo;
                var liquidacionDTO = resultado.liquidaciones().stream()
                        .filter(l -> Objects.equals(l.empleadoId(), finalId))
                        .findFirst()
                        .orElse(null);

                if (liquidacionDTO != null) {
                    Map<String, Object> reciboMap = new HashMap<>();
                    String[] partes = liquidacionDTO.nombreCompleto() != null
                            ? liquidacionDTO.nombreCompleto().split(" ", 2)
                            : new String[] { "", "" };
                    reciboMap.put("nombre", partes[0]);
                    reciboMap.put("apellido", partes.length > 1 ? partes[1] : "");
                    reciboMap.put("email", liquidacionDTO.email());
                    reciboMap.put("dni", liquidacionDTO.dni());
                    reciboMap.put("rol", liquidacionDTO.rol());
                    reciboMap.put("festival", f != null ? f.getNombre() : "Epicentro Gourmet 2026");
                    reciboMap.put("unidad", resultado.unidad());
                    reciboMap.put("diasFestival", resultado.diasFestival());
                    boolean esCajero = "Cajero".equalsIgnoreCase(liquidacionDTO.rol());
                    reciboMap.put("sueldoBase", liquidacionDTO.sueldoBase());
                    reciboMap.put("plusCategoria", esCajero ? null : liquidacionDTO.adicional());
                    reciboMap.put("montoAntiguedad", esCajero ? liquidacionDTO.adicional() : null);
                    reciboMap.put("aniosAntiguedad", liquidacionDTO.aniosAntiguedad());
                    reciboMap.put("coeficiente", liquidacionDTO.periodos() * 0.5);
                    reciboMap.put("total", liquidacionDTO.total());
                    reciboMap.put("fechaEmision", LocalDate.now());

                    model.addAttribute("recibo", reciboMap);
                } else if (esEmpleado) {
                    // Recibo ficticio de muestra para el empleado
                    Map<String, Object> reciboMap = new HashMap<>();
                    reciboMap.put("nombre", "Martín");
                    reciboMap.put("apellido", "González");
                    reciboMap.put("email", principal != null ? principal.getName() : "empleado@epicentro.com");
                    reciboMap.put("dni", "38452910");
                    reciboMap.put("rol", "Cajero");
                    reciboMap.put("festival", f != null ? f.getNombre() : "Epicentro Gourmet 2026");
                    reciboMap.put("unidad", "Food Truck 1 - El Asador Viajero");
                    reciboMap.put("diasFestival", 20L);
                    reciboMap.put("sueldoBase", new BigDecimal("100000"));
                    reciboMap.put("plusCategoria", null);
                    reciboMap.put("montoAntiguedad", new BigDecimal("15000"));
                    reciboMap.put("aniosAntiguedad", 3L);
                    reciboMap.put("coeficiente", 1.0);
                    reciboMap.put("total", new BigDecimal("115000"));
                    reciboMap.put("fechaEmision", LocalDate.now());

                    model.addAttribute("recibo", reciboMap);
                }

                model.addAttribute("liquidacion", liquidacionDTO);
                model.addAttribute("unidad", resultado.unidad());
                model.addAttribute("diasFestival", resultado.diasFestival());

                if (f != null) {
                    model.addAttribute("festival", f);
                }
            } else {
                model.addAttribute("error", "No se encontró un festival activo para ver el recibo.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo obtener el recibo: " + e.getMessage());
        }

        return "reportes/reciboPdf";
    }
}