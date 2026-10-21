package com.example.hito2grupo3.controllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final ApplicationContext applicationContext;

    public PedidoController(
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            ApplicationContext applicationContext) {

        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/alta")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute("festivales", festivalRepository.findAll());
        model.addAttribute("unidadesVenta", unidadVentaRepository.findAll());
        model.addAttribute("platos", List.of());
        model.addAttribute("fechaActual",
                LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));

        return "pedido/alta";
    }

    @GetMapping("/unidades/{id}/platos")
    @ResponseBody
    public ResponseEntity<List<PlatoRespuesta>> obtenerPlatos(
            @PathVariable Long id) {

        List<?> platosPorUnidad = invocarServicio("obtenerPlatosPorUnidad", id);
        List<PlatoRespuesta> platos = platosPorUnidad
                .stream()
            .map(plato -> {
                Plato p = (Plato) plato;
                return new PlatoRespuesta(p.getId(), p.getNombre(), p.getPrecio());
            })
                .toList();

        return ResponseEntity.ok(platos);
    }

    @PostMapping("/alta")
    public String registrarPedido(
            @ModelAttribute PedidoAltaDTO datos,
            RedirectAttributes redirectAttributes) {

        try {

            invocarServicio("registrarPedido", datos);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Pedido registrado correctamente.");

            return "redirect:/pedidos/alta";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());

            return "redirect:/pedidos/alta";
        }
    }

    public record PlatoRespuesta(
            Long id,
            String nombre,
            java.math.BigDecimal precio) {
    }

    @GetMapping("/festivales/{id}/unidades")
    @ResponseBody
    public ResponseEntity<List<UnidadRespuesta>> obtenerUnidadesPorFestival(
        @PathVariable Long id) {

        List<UnidadRespuesta> unidades = festivalRepository
            .obtenerUnidadesPorFestival(id)
            .stream()
            .map(unidad -> new UnidadRespuesta(
                    unidad.getId(),
                    unidad.getNombreComercial()))
            .toList();

        return ResponseEntity.ok(unidades);
    }

    public record UnidadRespuesta(Long id, String nombreComercial) {
    }

    @SuppressWarnings("unchecked")
    private <T> T invocarServicio(String metodo, Object argumento) {
        Object servicio = applicationContext.getBean("pedidoService");
        try {
            return (T) servicio.getClass()
                    .getMethod(metodo, argumento.getClass())
                    .invoke(servicio, argumento);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException ex) {
                throw ex;
            }
            throw new IllegalStateException("No se pudo ejecutar el servicio de pedidos", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo ejecutar el servicio de pedidos", e);
        }
    }

}
