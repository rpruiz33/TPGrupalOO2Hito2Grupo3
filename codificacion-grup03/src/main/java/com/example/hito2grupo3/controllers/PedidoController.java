package com.example.hito2grupo3.controllers;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import org.hibernate.mapping.Set;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
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
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final ApplicationContext applicationContext;

    public PedidoController(
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository,
            ApplicationContext applicationContext) {

        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/alta")
    public String mostrarFormularioAlta(
            Model model,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        Staff staff = obtenerStaffAutenticado(principal);

        LocalDate fechaActual = LocalDate.now(
                ZoneId.of("America/Argentina/Buenos_Aires"));

        model.addAttribute(
                "festivales",
                festivalRepository.buscarActivosPorStaff(
                        staff.getId(),
                        fechaActual));

        model.addAttribute("unidadesVenta", List.of());
        model.addAttribute("platos", List.of());
        model.addAttribute("fechaActual", fechaActual);

        return "pedido/alta";
    }

    @GetMapping("/festivales/{id}/unidades")
    @ResponseBody
    public ResponseEntity<List<UnidadRespuesta>> obtenerUnidadesPorFestival(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = obtenerStaffAutenticado(principal);

        LocalDate fechaActual = LocalDate.now(
                ZoneId.of("America/Argentina/Buenos_Aires"));

        boolean festivalDisponible = festivalRepository
                .buscarActivosPorStaff(staff.getId(), fechaActual)
                .stream()
                .anyMatch(festival -> festival.getId().equals(id));

        if (!festivalDisponible) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés acceso a este festival.");
        }

       List<UnidadRespuesta> unidades = festivalRepository
        .obtenerUnidadesPorFestival(id)
        .stream()
        .filter(unidad -> tieneStaffAsignado(unidad, staff))
        .map(this::toRespuesta)
        .toList();

return ResponseEntity.ok(unidades);
}

private boolean tieneStaffAsignado(UnidadVenta unidad, Staff staff) {
    Set asignados = (Set) unidad.getStaffAsignado();
    return asignados != null
            && ((Collection<Festival>) asignados).stream().anyMatch(s -> s.getId() == staff.getId());
}

private UnidadRespuesta toRespuesta(UnidadVenta unidad) {
    return new UnidadRespuesta(unidad.getId(), unidad.getNombreComercial());
}
    @GetMapping("/unidades/{id}/platos")
    @ResponseBody
    public ResponseEntity<List<PlatoRespuesta>> obtenerPlatos(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = obtenerStaffAutenticado(principal);

        boolean unidadAsignada = unidadVentaRepository
                .buscarPorStaffAsignado(staff.getId())
                .stream()
                .anyMatch(unidad -> unidad.getId().equals(id));

        if (!unidadAsignada) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés acceso a los platos de esta unidad de venta.");
        }

        List<?> platosPorUnidad = invocarServicio(
                "obtenerPlatosPorUnidad",
                id);

        List<PlatoRespuesta> platos = platosPorUnidad
                .stream()
                .map(plato -> {
                    Plato p = (Plato) plato;

                    return new PlatoRespuesta(
                            p.getId(),
                            p.getNombre(),
                            p.getPrecio());
                })
                .toList();

        return ResponseEntity.ok(platos);
    }

    @PostMapping("/alta")
    public String registrarPedido(
            @ModelAttribute PedidoAltaDTO datos,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Staff staff = obtenerStaffAutenticado(principal);

            invocarServicio(
                    "registrarPedido",
                    new Object[]{datos, staff.getId()});

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido registrado correctamente.");

            return "redirect:/pedidos/alta";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());

            return "redirect:/pedidos/alta";

        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getReason());

            return "redirect:/pedidos/alta";
        }
    }

    private Staff obtenerStaffAutenticado(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debés iniciar sesión.");
        }

        return staffRepository
                .findByUsuario_Email(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El usuario autenticado no tiene un empleado asociado."));
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

    /**
     * Invoca un método del servicio usando sus argumentos.
     * Permite invocar métodos con uno o varios parámetros.
     */
    @SuppressWarnings("unchecked")
    private <T> T invocarServicio(String metodo, Object... argumentos) {

        Object servicio = applicationContext.getBean("pedidoServiceImpl");

        try {
            Class<?>[] tipos = new Class<?>[argumentos.length];

            for (int i = 0; i < argumentos.length; i++) {
                tipos[i] = argumentos[i] instanceof Long
                        ? Long.class
                        : argumentos[i].getClass();
            }

            return (T) servicio.getClass()
                    .getMethod(metodo, tipos)
                    .invoke(servicio, argumentos);

        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException ex) {
                throw ex;
            }

            throw new IllegalStateException(
                    "No se pudo ejecutar el servicio de pedidos",
                    e.getCause());

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "No se pudo ejecutar el servicio de pedidos",
                    e);
        }
    }
}
