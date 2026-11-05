package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.services.ILiquidacionService;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','EMPLEADO','CAJERO','COCINERO')")
public class LiquidacionController {

    private final ILiquidacionService liquidacionService;

    public LiquidacionController(ILiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/liquidacion")
    public String liquidacion(
            @RequestParam(required = false) Long festivalId,
            @RequestParam(required = false) Long unidadId,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Double sueldo,
            @RequestParam(required = false, defaultValue = "false") boolean registradas,
            Authentication authentication,
            Principal principal,
            Model model) {

        boolean esAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esResponsable = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));

        boolean esEmpleado = !esAdmin && !esResponsable;

        boolean mostrarRegistradas = registradas && (esAdmin || esResponsable);

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esResponsable", esResponsable);
        model.addAttribute("esEmpleado", esEmpleado);
        model.addAttribute("registradas", mostrarRegistradas);

        Festival festival = liquidacionService.resolverFestivalPorDefecto(festivalId);

        model.addAttribute("festivales", liquidacionService.listarFestivales());
        model.addAttribute("festival", festival);

        Long idFestival = festival != null ? festival.getId() : null;
        model.addAttribute("festivalId", idFestival);

        if (esAdmin) {
            model.addAttribute(
                    "todasLasUnidades",
                    liquidacionService.listarTodasLasUnidades());
        }

        if (unidadId != null && unidadId > 0) {
            model.addAttribute("unidadId", unidadId);
        }

        model.addAttribute("codigo", codigo);

        if (idFestival != null && principal != null) {
            try {
                ILiquidacionService.ResultadoLiquidacion resultado;

                if (mostrarRegistradas) {
                    resultado = liquidacionService.consultarLiquidacionesRegistradasSegunRol(
                            principal.getName(),
                            idFestival,
                            unidadId,
                            codigo,
                            esAdmin,
                            esResponsable
                    );
                } else {
                    resultado = liquidacionService.liquidarSegunRol(
                            principal.getName(),
                            idFestival,
                            unidadId,
                            codigo,
                            esAdmin,
                            esResponsable
                    );
                }

                model.addAttribute("unidad", resultado.unidad());
                model.addAttribute("diasFestival", resultado.diasFestival());
                model.addAttribute("liquidaciones", resultado.liquidaciones());
                model.addAttribute("totalGeneral", resultado.totalGeneral());

                if (esEmpleado
                        && resultado.liquidaciones() != null
                        && !resultado.liquidaciones().isEmpty()) {

                    var miLiquidacion = resultado.liquidaciones().iterator().next();

                    model.addAttribute("empleadoNombre", miLiquidacion.nombreCompleto());
                    model.addAttribute("empleadoEmail", miLiquidacion.email());
                }

            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "reportes/liquidacion";
    }

    /**
     * Muestra el recibo de una liquidación individual ya registrada.
     */
    @GetMapping({
            "/reciboPdf/{id}",
            "/liquidacion/recibo/{id}",
            "/empleado/recibo/{id}/liquidaciones"
    })
    public String verReciboLiquidacion(
            @PathVariable("id") Long liquidacionId,
            Authentication authentication,
            Principal principal,
            Model model) {

        boolean esAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esResponsable = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esResponsable", esResponsable);

        try {
            String email = principal != null ? principal.getName() : null;

            var reciboData = liquidacionService.obtenerModeloRecibo(
                    liquidacionId,
                    email,
                    esAdmin,
                    esResponsable
            );

            model.addAllAttributes(reciboData);

        } catch (Exception e) {
            model.addAttribute(
                    "error",
                    "No se pudo obtener el recibo: " + e.getMessage()
            );
        }

        return "reportes/reciboPdf";
    }

    /**
     * Registra las liquidaciones únicamente cuando el administrador
     * o responsable confirma la operación.
     */
    @PostMapping("/liquidacion/registrar")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public String registrarLiquidaciones(
            @RequestParam Long festivalId,
            @RequestParam(required = false) Long unidadId,
            @RequestParam(required = false) String codigo,
            Authentication authentication,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        boolean esAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esResponsable = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSABLE"));

        try {
            var resultado = liquidacionService.registrarLiquidacionesSegunRol(
                    principal != null ? principal.getName() : null,
                    festivalId,
                    unidadId,
                    codigo,
                    esAdmin,
                    esResponsable
            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La registración finalizó para "
                            + resultado.liquidaciones().size()
                            + " liquidación(es)."
            );

            // Después de registrar, consultar los registros persistidos.
            redirectAttributes.addAttribute("registradas", true);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        redirectAttributes.addAttribute("festivalId", festivalId);

        if (unidadId != null && unidadId > 0) {
            redirectAttributes.addAttribute("unidadId", unidadId);
        }

        if (codigo != null && !codigo.isBlank()) {
            redirectAttributes.addAttribute("codigo", codigo);
        }

        return "redirect:/liquidacion";
    }
}