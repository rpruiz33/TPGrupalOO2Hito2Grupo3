package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.stream.StreamSupport;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.StreamSupport;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final PedidoRepository pedidoRepository;

    public AdminController(FestivalRepository festivalRepository,
                           UsuarioRepository usuarioRepository,
                           UnidadVentaRepository unidadVentaRepository,
                           StaffRepository staffRepository,
                           PedidoRepository pedidoRepository) {
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.pedidoRepository = pedidoRepository;
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

@GetMapping("/festivales")
public String listarFestivales(Model model) {
    model.addAttribute("festivales", festivalRepository.findAll());
    return "admin/festivales";
}

@GetMapping("/festivales/{id}")
@Transactional(readOnly = true)
public String verUnidadesFestival(@PathVariable Long id, Model model) {
    Festival festival = festivalRepository.findById(id)
            .orElse(null);

    if (festival == null) {
        return "redirect:/admin/festivales";
    }

    model.addAttribute("festival", festival);
    model.addAttribute(
            "unidades",
            festival.getUnidadesVentaHabilitadas()
    );

    return "admin/unidades-festivales";
}
}
