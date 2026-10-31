package com.example.hito2grupo3.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IPedidoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final ZoneId ZONA_HORARIA =
            ZoneId.of("America/Argentina/Buenos_Aires");

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final IPedidoService pedidoService;

    public PedidoController(
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository,
            IPedidoService pedidoService) {

        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
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

        Staff staff = obtenerStaffAutenticado(principal);

        verificarRolPedidos(staff);

        LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);

        // Obtener festivales vigentes donde el usuario
        // tenga acceso a alguna unidad de venta.
        List<Festival> festivalesDisponibles =
                festivalRepository.findAllByOrderByFechaInicioAsc()
                        .stream()
                        .filter(festival ->
                                estaVigente(festival, fechaActual))
                        .filter(festival ->
                                festivalRepository
                                        .obtenerUnidadesPorFestival(
                                                festival.getId())
                                        .stream()
                                        .anyMatch(unidad ->
                                                tieneAccesoUnidad(
                                                        unidad, staff)))
                        .toList();

        model.addAttribute("festivales", festivalesDisponibles);
        model.addAttribute("unidadesVenta", List.of());
        model.addAttribute("platos", List.of());
        model.addAttribute("fechaActual", fechaActual);

        return "pedido/alta";
    }

    @PostMapping("/alta")
    public String registrarPedido(
            @ModelAttribute PedidoAltaDTO datos,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Staff staff = obtenerStaffAutenticado(principal);

            pedidoService.registrarPedido(datos, staff.getId());

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

    // ============================
    // UNIDADES POR FESTIVAL
    // ============================

    @GetMapping("/festivales/{id}/unidades")
    @ResponseBody
    public ResponseEntity<List<UnidadRespuesta>> obtenerUnidadesPorFestival(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = obtenerStaffAutenticado(principal);

        verificarRolPedidos(staff);

        LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);

        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe el festival seleccionado."));

        if (!estaVigente(festival, fechaActual)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El festival no está vigente.");
        }

        List<UnidadRespuesta> unidades = festivalRepository
                .obtenerUnidadesPorFestival(id)
                .stream()
                .filter(unidad ->
                        tieneAccesoUnidad(unidad, staff))
                .map(this::toRespuesta)
                .toList();

        return ResponseEntity.ok(unidades);
    }

    // ============================
    // PLATOS POR UNIDAD
    // ============================

    @GetMapping("/unidades/{id}/platos")
    @ResponseBody
    public ResponseEntity<List<PlatoRespuesta>> obtenerPlatos(
            @PathVariable Long id,
            Principal principal) {

        Staff staff = obtenerStaffAutenticado(principal);

        verificarAccesoUnidad(id, staff);

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

        Staff staff = obtenerStaffAutenticado(principal);

        verificarRolPedidos(staff);

        LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe el festival seleccionado."));

        if (!estaVigente(festival, fechaActual)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El festival no está vigente.");
        }

        boolean unidadDelFestival = festivalRepository
                .obtenerUnidadesPorFestival(festivalId)
                .stream()
                .anyMatch(unidad ->
                        Objects.equals(unidad.getId(), unidadVentaId));

        if (!unidadDelFestival) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La unidad de venta no pertenece a este festival.");
        }

        verificarAccesoUnidad(unidadVentaId, staff);

        PlatoDTO platoEstrella = pedidoService.obtenerPlatoEstrella(
                unidadVentaId,
                festivalId);

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

        Staff staff = obtenerStaffAutenticado(principal);

        String rol = obtenerRol(staff);

        List<UnidadVenta> unidadesVenta;

        if ("RESPONSABLE".equals(rol)) {

            unidadesVenta = unidadVentaRepository
                    .buscarPorStaffAsignadoOResponsable(staff.getId());

        } else if ("CAJERO".equals(rol)) {

            unidadesVenta = unidadVentaRepository
                    .buscarPorStaffAsignado(staff.getId());

        } else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés autorización para consultar pedidos.");
        }

        // Obtener el historial permitido para el usuario.
        List<Pedido> pedidos =
                pedidoService.obtenerHistorialPorStaff(staff.getId());

        // ==========================================
        // EMPLEADOS QUE REGISTRARON PEDIDOS
        // ==========================================

        // Obtener los empleados directamente desde los pedidos.
        // No se filtra por estado de cuenta ni por asignaciones actuales.
        // Así se conserva la trazabilidad histórica.

        Map<Long, Staff> empleadosPorId = new LinkedHashMap<>();

        for (Pedido pedido : pedidos) {

            Staff registrador = pedido.getRegistradoPor();
if (registrador != null) {
    empleadosPorId.putIfAbsent(
            registrador.getId(),
            registrador);
}
        }

        // Crear lista única de empleados.
        List<Staff> empleados =
                new ArrayList<>(empleadosPorId.values());

        // Orden alfabético por apellido y nombre.
        empleados.sort(
                Comparator.comparing(
                        Staff::getApellido,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(
                                Staff::getNombre,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER))
        );

        // Enviar los datos a Thymeleaf.
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("unidadesVenta", unidadesVenta);
        model.addAttribute("empleados", empleados);
        model.addAttribute("rol", rol);

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

        Staff staff = obtenerStaffAutenticado(principal);

        String rol = obtenerRol(staff);

        if (!"RESPONSABLE".equals(rol)
                && !"CAJERO".equals(rol)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés autorización para consultar festivales.");
        }

        verificarAccesoUnidad(id, staff);

        LocalDate hoy = LocalDate.now(ZONA_HORARIA);

        List<FestivalRespuesta> festivales = festivalRepository
                .buscarTodosPorUnidadVenta(id)
                .stream()
                // El responsable ve todos los festivales.
                // El cajero solamente los vigentes.
                .filter(festival ->
                        "RESPONSABLE".equals(rol)
                        || estaVigente(festival, hoy))
                .map(festival -> new FestivalRespuesta(
                        festival.getId(),
                        festival.getNombre(),
                        festival.getFechaInicio(),
                        festival.getFechaFin()))
                .toList();

        return ResponseEntity.ok(festivales);
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
            Staff staff = obtenerStaffAutenticado(principal);

            pedidoService.anularPedido(
                    id,
                    staff.getId(),
                    motivo);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El pedido fue anulado correctamente.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());

        } catch (ResponseStatusException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getReason());
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

        Staff staff = obtenerStaffAutenticado(principal);

        Pedido pedido = pedidoService.obtenerPedidoPorStaff(
                id,
                staff.getId());

        model.addAttribute("pedido", pedido);

        return "pedido/detalle";
    }

    // ============================
    // VALIDACIONES DE ACCESO
    // ============================

    private void verificarAccesoUnidad(
            Long unidadVentaId,
            Staff staff) {

        String rol = obtenerRol(staff);

        List<UnidadVenta> unidades;

        if ("RESPONSABLE".equals(rol)) {

            unidades = unidadVentaRepository
                    .buscarPorStaffAsignadoOResponsable(staff.getId());

        } else if ("CAJERO".equals(rol)) {

            unidades = unidadVentaRepository
                    .buscarPorStaffAsignado(staff.getId());

        } else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés autorización para acceder a pedidos.");
        }

        boolean tieneAcceso = unidades.stream()
                .anyMatch(unidad ->
                        Objects.equals(unidad.getId(), unidadVentaId));

        if (!tieneAcceso) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés acceso a esta unidad de venta.");
        }
    }

    private boolean tieneAccesoUnidad(
            UnidadVenta unidad,
            Staff staff) {

        String rol = obtenerRol(staff);

        boolean asignado = unidad.getStaffAsignado() != null
                && unidad.getStaffAsignado().stream()
                        .anyMatch(s ->
                                Objects.equals(
                                        s.getId(),
                                        staff.getId()));

        if ("RESPONSABLE".equals(rol)) {

            boolean esResponsableDeUnidad =
                    unidad.getResponsable() != null
                    && Objects.equals(
                            unidad.getResponsable().getId(),
                            staff.getId());

            return asignado || esResponsableDeUnidad;
        }

        return "CAJERO".equals(rol) && asignado;
    }

    private boolean estaVigente(
            Festival festival,
            LocalDate fecha) {

        return festival != null
                && festival.getFechaInicio() != null
                && festival.getFechaFin() != null
                && !fecha.isBefore(festival.getFechaInicio())
                && !fecha.isAfter(festival.getFechaFin());
    }

    private void verificarRolPedidos(Staff staff) {

        String rol = obtenerRol(staff);

        if (!"RESPONSABLE".equals(rol)
                && !"CAJERO".equals(rol)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tenés autorización para gestionar pedidos.");
        }
    }

    private String obtenerRol(Staff staff) {

        if (staff.getUsuario() == null
                || staff.getUsuario().getRol() == null) {

            throw new IllegalArgumentException(
                    "El usuario no tiene un rol asignado.");
        }

        return staff.getUsuario().getRol().getNombre()
                .trim().toUpperCase();
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

    // ============================
    // RESPUESTAS
    // ============================

    private UnidadRespuesta toRespuesta(UnidadVenta unidad) {

        return new UnidadRespuesta(
                unidad.getId(),
                unidad.getNombreComercial());
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