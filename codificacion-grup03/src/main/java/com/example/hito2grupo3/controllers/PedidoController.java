package com.example.hito2grupo3.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.services.IPedidoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final IPedidoService pedidoService;

    public PedidoController(IPedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // ============================
    // ALTA DE PEDIDOS
    // ============================

    @GetMapping("/alta")
    public String mostrarFormularioAlta(
            Model model,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);

        List<Festival> festivalesDisponibles =
                pedidoService.obtenerFestivalesDisponiblesParaAlta(staff.getId());

        model.addAttribute("festivales", festivalesDisponibles);
        model.addAttribute("unidadesVenta", List.of());
        model.addAttribute("platos", List.of());
        model.addAttribute("fechaActual", fechaActual);

        return "pedido/alta";
    }

    @PostMapping("/alta")
    public String registrarPedido(
            @jakarta.validation.Valid @ModelAttribute("datos") PedidoAltaDTO datos,
            org.springframework.validation.BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .orElse("Datos del pedido inválidos.");
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/pedidos/alta";
        }

        try {
            Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
            pedidoService.registrarPedido(datos, staff.getId());

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido registrado correctamente.");

            return "redirect:/pedidos/alta";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/alta";

        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getReason());
            return "redirect:/pedidos/alta";
        }
    }

    // ============================
    // UNIDADES POR FESTIVAL
    // ============================

    @GetMapping("/festivales/{id}/unidades")
    @ResponseBody
    public ResponseEntity<List<UnidadRespuesta>> obtenerUnidadesPorFestival(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        List<UnidadVenta> unidades = pedidoService.obtenerUnidadesDisponiblesPorFestival(id, staff.getId());

        List<UnidadRespuesta> respuesta = unidades.stream()
                .map(this::toRespuesta)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // ============================
    // PLATOS POR UNIDAD
    // ============================

    @GetMapping("/unidades/{id}/platos")
    @ResponseBody
    public ResponseEntity<List<PlatoRespuesta>> obtenerPlatos(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        pedidoService.verificarAccesoUnidad(id, staff.getId());

        List<PlatoRespuesta> platos = pedidoService
                .obtenerPlatosPorUnidad(id)
                .stream()
                .map(plato -> new PlatoRespuesta(
                        plato.getId(),
                        plato.getNombre(),
                        plato.getPrecio()))
                .toList();

        return ResponseEntity.ok(platos);
    }

    // ============================
    // PLATO ESTRELLA
    // ============================

    @GetMapping("/unidades/{unidadVentaId}/festivales/{festivalId}/plato-estrella")
    @ResponseBody
    public ResponseEntity<PlatoDTO> obtenerPlatoEstrella(
            @PathVariable Long unidadVentaId,
            @PathVariable Long festivalId,
            Principal principal) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        PlatoDTO platoEstrella = pedidoService.obtenerPlatoEstrellaValidado(unidadVentaId, festivalId, staff.getId());

        if (platoEstrella == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(platoEstrella);
    }

    // ============================
    // HISTORIAL DE PEDIDOS
    // ============================

    @GetMapping("/historial")
    public String mostrarHistorial(
            Model model,
            Principal principal) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        Map<String, Object> datosHistorial = pedidoService.obtenerDatosHistorial(staff.getId());
        model.addAllAttributes(datosHistorial);

        return "pedido/historial";
    }

    // ============================
    // FESTIVALES POR UNIDAD
    // ============================

    @GetMapping("/unidades/{id}/festivales")
    @ResponseBody
    public ResponseEntity<List<FestivalRespuesta>> obtenerFestivalesPorUnidad(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        List<Festival> festivales = pedidoService.obtenerFestivalesPorUnidadYStaff(id, staff.getId());

        List<FestivalRespuesta> respuesta = festivales.stream()
                .map(festival -> new FestivalRespuesta(
                        festival.getId(),
                        festival.getNombre(),
                        festival.getFechaInicio(),
                        festival.getFechaFin()))
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // ============================
    // ANULAR PEDIDO
    // ============================

    @PostMapping("/{id}/anular")
    public String anularPedido(
            @PathVariable Long id,
            @ModelAttribute("motivo") String motivo,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
            pedidoService.anularPedido(id, staff.getId(), motivo);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El pedido fue anulado correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getReason());
        }

        return "redirect:/pedidos/historial";
    }

    // ============================
    // DETALLE DEL PEDIDO
    // ============================

    @GetMapping("/{id}")
    public String mostrarDetallePedido(
            @PathVariable Long id,
            Principal principal,
            Model model) {

        Staff staff = pedidoService.obtenerStaffPorEmail(principal.getName());
        Pedido pedido = pedidoService.obtenerPedidoPorStaff(id, staff.getId());
        model.addAttribute("pedido", pedido);

        return "pedido/detalle";
    }

    // ============================
    // RESPUESTAS DTO
    // ============================

    private UnidadRespuesta toRespuesta(UnidadVenta unidad) {
        return new UnidadRespuesta(unidad.getId(), unidad.getNombreComercial());
    }

    public record PlatoRespuesta(
            Long id,
            String nombre,
            java.math.BigDecimal precio) {
    }

    public record UnidadRespuesta(
            Long id,
            String nombreComercial) {
    }

    public record FestivalRespuesta(
            Long id,
            String nombre,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
    }
}