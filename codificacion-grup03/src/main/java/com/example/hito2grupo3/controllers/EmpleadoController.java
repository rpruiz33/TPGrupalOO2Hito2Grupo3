
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final FestivalRepository festivalRepository;

    public EmpleadoController(StaffRepository staffRepository,
                              UnidadVentaRepository unidadVentaRepository,
                              FestivalRepository festivalRepository) {
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.festivalRepository = festivalRepository;
    }

    @GetMapping("/index")
    public String mostrarIndex() {
        return "empleado/index";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Principal principal, Model model) {
        String email = principal != null ? principal.getName() : "empleado@epicentro.com";

        // Buscar staff por email del usuario logueado o tomar el primero disponible
        Staff staff = staffRepository.findByUsuario_Email(email)
                .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElse(null));

        if (staff != null) {
            long antiguedad = staff.getFechaIngreso() != null ?
                    ChronoUnit.YEARS.between(staff.getFechaIngreso(), LocalDate.now()) : 0;
            boolean esCajero = staff instanceof Cajero;
            String rol = esCajero ? "Cajero" : (staff instanceof Cocinero c && c.getCategoriaCocina() != null ? c.getCategoriaCocina().getNombre() : "Staff General");

            Map<String, Object> empleadoMap = new HashMap<>();
            empleadoMap.put("id", staff.getId());
            empleadoMap.put("nombre", staff.getNombre() + " " + staff.getApellido());
            empleadoMap.put("dni", staff.getDni());
            empleadoMap.put("rol", rol);
            empleadoMap.put("antiguedad", antiguedad);
            empleadoMap.put("cajero", esCajero);
            model.addAttribute("empleado", empleadoMap);

            // Asignación actual: buscar si es responsable o staff asignado de alguna unidad
            UnidadVenta unidad = unidadVentaRepository.findAll().stream()
                    .filter(u -> (u.getResponsable() != null && u.getResponsable().getId() == staff.getId())
                            || (u.getStaffAsignado() != null && u.getStaffAsignado().stream().anyMatch(s -> s.getId() == staff.getId())))
                    .findFirst()
                    .orElse(null);

            Festival festival = festivalRepository.findAll().stream().findFirst().orElse(null);

            if (unidad != null) {
                Map<String, Object> asignacion = new HashMap<>();
                asignacion.put("unidad", unidad.getNombreComercial());
                asignacion.put("festival", festival != null ? festival.getNombre() : "Epicentro Gourmet UNLa 2026");
                model.addAttribute("asignacionActual", asignacion);
            }

            // Historial de unidades y festivales
            List<Map<String, Object>> historialList = new ArrayList<>();
            List<UnidadVenta> todasUnidades = unidadVentaRepository.findAll();
            for (UnidadVenta uv : todasUnidades) {
                for (Festival f : uv.getFestivales().isEmpty() && festival != null ? List.of(festival) : uv.getFestivales()) {
                    Map<String, Object> h = new HashMap<>();
                    h.put("id", staff.getId());
                    h.put("festivalId", f.getId());
                    h.put("festival", f.getNombre());
                    h.put("unidad", uv.getNombreComercial());
                    h.put("sueldo", staff.getSueldo() != null ? staff.getSueldo() : 100000.00);
                    historialList.add(h);
                }
            }
            if (!historialList.isEmpty()) {
                model.addAttribute("historial", historialList);
            }
        }

        return "reportes/empleadoDashboard";
    }
}
