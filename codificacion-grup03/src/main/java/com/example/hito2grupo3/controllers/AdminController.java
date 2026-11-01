package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.services.IModificarUnidadVentaService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final PedidoRepository pedidoRepository;
    private final IModificarUnidadVentaService modificarUnidadVentaService;

    public AdminController(FestivalRepository festivalRepository,
                           UsuarioRepository usuarioRepository,
                           UnidadVentaRepository unidadVentaRepository,
                           StaffRepository staffRepository,
                           PedidoRepository pedidoRepository,
                           IModificarUnidadVentaService modificarUnidadVentaService) {
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.pedidoRepository = pedidoRepository;
        this.modificarUnidadVentaService = modificarUnidadVentaService;
    }

    @GetMapping("/dashboard")
    public String panelAdministrador(Model model) {
        // 1. Festivales registrados reales
        long festivalesCount = festivalRepository.count();

        // 2. Usuarios registrados reales en el sistema
        long usuariosCount = usuarioRepository.count();

        // 3. Recaudación real calculada a partir de los pedidos en base de datos
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        BigDecimal recaudacionMes = pedidoRepository.calcularRecaudacionEntreFechas(inicioMes, finMes);
        BigDecimal recaudacionTotal = pedidoRepository.calcularRecaudacionTotal();

        BigDecimal recaudacionMostrar = (recaudacionMes != null && recaudacionMes.compareTo(BigDecimal.ZERO) > 0)
                ? recaudacionMes
                : (recaudacionTotal != null ? recaudacionTotal : BigDecimal.ZERO);

        // 4. Unidades de venta reales
        long unidadesCount = unidadVentaRepository.count();

        // 5. Personal / Staff real
        long staffCount = staffRepository.count();

        Long primerFestivalId = festivalRepository.findFirstByOrderByIdAsc()
                .map(Festival::getId)
                .orElse(1L);
        model.addAttribute("festivalesCount", festivalesCount);
        model.addAttribute("usuariosCount", usuariosCount);
        model.addAttribute("recaudacionMensual", recaudacionMostrar);
        model.addAttribute("unidadesCount", unidadesCount);
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("primerFestivalId", primerFestivalId);

        return "admin/dashboard";
    }

    @GetMapping("/unidades/{id}/editar")
    @Transactional(readOnly = true)
    public String mostrarFormularioEdicion(
            @PathVariable Long id,
            @RequestParam Long festivalId,
            Model model) {

        UnidadVenta unidad = unidadVentaRepository.findById(id)
                .orElse(null);

        if (unidad == null) {
            return "redirect:/admin/festivales";
        }

        model.addAttribute("unidad", unidad);
        model.addAttribute("esFoodTruck", unidad instanceof FoodTruck);
        model.addAttribute("festivalId", festivalId);

        return "admin/modificar-unidad";
    }

    @PostMapping("/unidades/{id}/editar")
    public String modificarUnidad(
            @PathVariable Long id,
            @RequestParam String nombreComercial,
            @RequestParam BigDecimal superficieM2,
            @RequestParam(required = false) String patente,
            @RequestParam(required = false) Boolean requiereElectricidad,
            @RequestParam(required = false) Integer cantidadCarpas,
            @RequestParam(required = false) Integer tiempoMontajeMin,
            @RequestParam(required = false) Long festivalId,
            RedirectAttributes redirectAttributes) {

        try {
            modificarUnidadVentaService.modificar(
                    id,
                    nombreComercial,
                    superficieM2,
                    patente,
                    requiereElectricidad,
                    cantidadCarpas,
                    tiempoMontajeMin);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "La unidad de venta se modificó correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            if (festivalId != null) {
                return "redirect:/admin/unidades/" + id
                        + "/editar?festivalId=" + festivalId;
            }

            return "redirect:/admin/festivales";
        }

        if (festivalId != null) {
            return "redirect:/admin/festivales/" + festivalId;
        }

        return "redirect:/admin/festivales";
    }
}

