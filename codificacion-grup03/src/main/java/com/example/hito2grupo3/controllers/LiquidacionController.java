
package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.services.ILiquidacionService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
            @RequestParam(defaultValue = "false") boolean registradas,
            @RequestParam(defaultValue = "false") boolean personal,
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

        // El responsable también puede consultar sus propios haberes.
        boolean vistaPersonalResponsable = esResponsable && personal;

        model.addAttribute("esAdmin", esAdmin);

        model.addAttribute(
                "esResponsable",
                esResponsable && !vistaPersonalResponsable);

        model.addAttribute(
                "esEmpleado",
                esEmpleado || vistaPersonalResponsable);

        boolean mostrarRegistradas = registradas;

        model.addAttribute("registradas", mostrarRegistradas);

        Festival festival =
                liquidacionService.resolverFestivalPorDefecto(festivalId);

        model.addAttribute(
                "festivales",
                liquidacionService.listarFestivales());

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

                if (vistaPersonalResponsable) {

                    // El responsable consulta únicamente sus propios haberes.
                    resultado = liquidacionService.liquidarEmpleado(
                            principal.getName(),
                            idFestival);

                } else if (esAdmin || esResponsable) {

                    if (registradas) {

                        // Consulta explícita de liquidaciones registradas.
                        resultado =
                                liquidacionService.consultarLiquidacionesRegistradasSegunRol(
                                        principal.getName(),
                                        idFestival,
                                        unidadId,
                                        codigo,
                                        esAdmin,
                                        esResponsable);

                    } else {

                        // Calcular la vista previa del personal correspondiente.
                        ILiquidacionService.ResultadoLiquidacion vistaPrevia =
                                liquidacionService.liquidarSegunRol(
                                        principal.getName(),
                                        idFestival,
                                        unidadId,
                                        codigo,
                                        esAdmin,
                                        esResponsable);

                        // Consultar los registros existentes en la base de datos.
                        ILiquidacionService.ResultadoLiquidacion existentes =
                                liquidacionService.consultarLiquidacionesRegistradasSegunRol(
                                        principal.getName(),
                                        idFestival,
                                        unidadId,
                                        codigo,
                                        esAdmin,
                                        esResponsable);

                        // Comprobar si todos los períodos de la vista previa
                        // ya están cubiertos por liquidaciones registradas.
                        if (todasLasLiquidacionesEstanRegistradas(
                                vistaPrevia,
                                existentes)) {

                            resultado = existentes;
                            mostrarRegistradas = true;

                        } else {

                            // Si falta algún período, mantener la vista previa
                            // para permitir completar la registración.
                            resultado = vistaPrevia;
                            mostrarRegistradas = false;
                        }
                    }

                } else {

                    // Los empleados consultan sus propios haberes.
                    resultado = liquidacionService.liquidarSegunRol(
                            principal.getName(),
                            idFestival,
                            unidadId,
                            codigo,
                            esAdmin,
                            esResponsable);
                }

                model.addAttribute("registradas", mostrarRegistradas);

                model.addAttribute("unidad", resultado.unidad());
                model.addAttribute("diasFestival", resultado.diasFestival());
                model.addAttribute("liquidaciones", resultado.liquidaciones());
                model.addAttribute("totalGeneral", resultado.totalGeneral());

                // Identificar al empleado en su consulta individual.
                if ((esEmpleado || vistaPersonalResponsable)
                        && resultado.liquidaciones() != null
                        && !resultado.liquidaciones().isEmpty()) {

                    var miLiquidacion =
                            resultado.liquidaciones().iterator().next();

                    model.addAttribute(
                            "empleadoNombre",
                            miLiquidacion.nombreCompleto());

                    model.addAttribute(
                            "empleadoEmail",
                            miLiquidacion.email());
                }

            } catch (IllegalArgumentException e) {

                model.addAttribute("error", e.getMessage());
            }
        }

        return "reportes/liquidacion";
    }

    /**
     * Comprueba si todos los empleados y períodos de la vista previa
     * ya tienen su correspondiente liquidación persistida.
     */
    private boolean todasLasLiquidacionesEstanRegistradas(
            ILiquidacionService.ResultadoLiquidacion vistaPrevia,
            ILiquidacionService.ResultadoLiquidacion existentes) {

        if (vistaPrevia == null
                || vistaPrevia.liquidaciones() == null
                || existentes == null
                || existentes.liquidaciones() == null
                || existentes.liquidaciones().isEmpty()) {

            return false;
        }

        // Si no hay personal activo para calcular, pero existen registros,
        // mostrar los registros históricos.
        if (vistaPrevia.liquidaciones().isEmpty()) {
            return true;
        }

        return vistaPrevia.liquidaciones().stream().allMatch(prevision ->
                periodoCubiertoPorRegistros(
                        prevision,
                        existentes.liquidaciones())
        );
    }

    /**
     * Comprueba si uno o varios registros guardados cubren
     * completamente el período de una liquidación calculada.
     *
     * Contempla registros históricos cuyos períodos tengan
     * límites diferentes a los de la vista previa.
     */
    private boolean periodoCubiertoPorRegistros(
            LiquidacionDTO prevision,
            Set<LiquidacionDTO> existentes) {

        if (prevision == null
                || prevision.empleadoId() == null
                || prevision.unidadId() == null
                || prevision.periodoInicio() == null
                || prevision.periodoFin() == null) {

            return false;
        }

        List<LiquidacionDTO> registrosDelEmpleado = new ArrayList<>();

        for (LiquidacionDTO registro : existentes) {

            if (registro.liquidacionId() == null
                    || !Objects.equals(
                            prevision.empleadoId(),
                            registro.empleadoId())
                    || !Objects.equals(
                            prevision.unidadId(),
                            registro.unidadId())
                    || registro.periodoInicio() == null
                    || registro.periodoFin() == null) {

                continue;
            }

            registrosDelEmpleado.add(registro);
        }

        if (registrosDelEmpleado.isEmpty()) {
            return false;
        }

        registrosDelEmpleado.sort(
                Comparator.comparing(LiquidacionDTO::periodoInicio));

        LocalDate cursor = prevision.periodoInicio();
        LocalDate finNecesario = prevision.periodoFin();

        for (LiquidacionDTO registro : registrosDelEmpleado) {

            LocalDate inicioRegistro = registro.periodoInicio();
            LocalDate finRegistro = registro.periodoFin();

            // Ignorar registros anteriores al tramo pendiente.
            if (finRegistro.isBefore(cursor)) {
                continue;
            }

            // Existe un espacio de días que todavía no está cubierto.
            if (inicioRegistro.isAfter(cursor)) {
                return false;
            }

            // Avanzar solamente si el registro amplía el período cubierto.
            LocalDate siguienteDia = finRegistro.plusDays(1);

            if (siguienteDia.isAfter(cursor)) {
                cursor = siguienteDia;
            }

            // El período solicitado está completamente cubierto.
            if (cursor.isAfter(finNecesario)) {
                return true;
            }
        }

        return false;
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

        boolean esEmpleado = !esAdmin && !esResponsable;

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("esResponsable", esResponsable);
        model.addAttribute("esEmpleado", esEmpleado);

        try {

            String email = principal != null
                    ? principal.getName()
                    : null;

            Map<String, Object> reciboData =
                    liquidacionService.obtenerModeloRecibo(
                            liquidacionId,
                            email,
                            esAdmin,
                            esResponsable);

            model.addAllAttributes(reciboData);

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "No se pudo obtener el recibo: " + e.getMessage());
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

        boolean registroExitoso = false;

        try {

            var resultado =
                    liquidacionService.registrarLiquidacionesSegunRol(
                            principal != null ? principal.getName() : null,
                            festivalId,
                            unidadId,
                            codigo,
                            esAdmin,
                            esResponsable);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La registración finalizó para "
                            + resultado.liquidaciones().size()
                            + " liquidación(es).");

            registroExitoso = true;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        redirectAttributes.addAttribute("festivalId", festivalId);

        if (unidadId != null && unidadId > 0) {
            redirectAttributes.addAttribute("unidadId", unidadId);
        }

        if (codigo != null && !codigo.isBlank()) {
            redirectAttributes.addAttribute("codigo", codigo);
        }

        // Después de registrar correctamente, consultar lo guardado.
        if (registroExitoso) {
            redirectAttributes.addAttribute("registradas", true);
        }

        return "redirect:/liquidacion";
    }
}
