package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.AsignacionStaffDTO;
import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.Cocinero;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.AsignacionStaffRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IEmpleadoInfoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EmpleadoInfoServiceImpl implements IEmpleadoInfoService {

    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final AsignacionStaffRepository asignacionStaffRepository;

    public EmpleadoInfoServiceImpl(
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            AsignacionStaffRepository asignacionStaffRepository) {

        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.asignacionStaffRepository = asignacionStaffRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosEmpleado(String email) {

        Staff staff = staffRepository.findByUsuario_Email(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un empleado asociado al usuario " + email
                                + ". Contactá con el administrador."));

        String rol = resolverRol(staff);
String puesto = rol;

if (staff instanceof Cocinero cocinero) {
    puesto = cocinero.getCategoriaCocina() != null
            && cocinero.getCategoriaCocina().getNombre() != null
                    ? cocinero.getCategoriaCocina().getNombre()
                    : "Categoría sin asignar";
}
        boolean esCajero = "Cajero".equals(rol)
                && staff instanceof Cajero;

        String antiguedad = calcularAntiguedad(staff.getFechaIngreso());

        Map<String, Object> empleadoMap = new HashMap<>();

        empleadoMap.put("id", staff.getId());
        empleadoMap.put("nombre", staff.getNombre() + " " + staff.getApellido());
        empleadoMap.put("dni", staff.getDni());
        empleadoMap.put("rol", rol);
        empleadoMap.put("puesto", puesto);
        empleadoMap.put("antiguedad", antiguedad);
        empleadoMap.put("cajero", esCajero);
        empleadoMap.put("email", email);

        if (esCajero) {
            Cajero cajero = (Cajero) staff;

            empleadoMap.put("turno",
                    cajero.getTurno() != null
                            ? cajero.getTurno()
                            : "No asignado");

            empleadoMap.put("numeroCaja",
                    cajero.getNumeroCaja() != null
                            ? cajero.getNumeroCaja()
                            : "No asignada");
        }

        return empleadoMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerAsignacionActual(String email) {

        Staff staff = staffRepository.findByUsuario_Email(email).orElse(null);

        if (staff == null) {
            return asignacionVacia();
        }

        LocalDate hoy = LocalDate.now();

        UnidadVenta unidadVigente = unidadVentaRepository.findAll()
                .stream()
                .filter(u -> estaAsignadoAUnidad(u, staff))
                .filter(u -> tieneFestivalVigente(u, hoy))
                .findFirst()
                .orElse(null);

        Map<String, Object> asignacion = new HashMap<>();

        if (unidadVigente != null) {

            var festivalesVigentes = unidadVigente.getFestivales().stream()
                    .filter(f -> esFestivalVigente(f, hoy))
                    .toList();

            asignacion.put("unidad", unidadVigente.getNombreComercial());

            asignacion.put("festival", festivalesVigentes.isEmpty()
                    ? "Sin festival vigente"
                    : festivalesVigentes.get(0).getNombre());

        } else {
            return asignacionVacia();
        }

        return asignacion;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDashboardEmpleado(String email) {

        Map<String, Object> modelMap = new HashMap<>();

        Map<String, Object> empleadoMap = obtenerDatosEmpleado(email);
        modelMap.put("empleado", empleadoMap);

        Staff staff = staffRepository.findByUsuario_Email(email).orElse(null);

        if (staff == null) {

            modelMap.put("asignacionActual", asignacionVacia());
            modelMap.put("unidadAsignada", null);
            modelMap.put("festivalesAsignados", Collections.emptyList());
            modelMap.put("historialAsignaciones", Collections.emptyList());

            return modelMap;
        }

        LocalDate hoy = LocalDate.now();

        UnidadVenta unidadVigente = unidadVentaRepository.findAll()
                .stream()
                .filter(u -> estaAsignadoAUnidad(u, staff))
                .findFirst()
                .orElse(null);

        if (unidadVigente != null) {

            var festivalesVigentes = unidadVigente.getFestivales().stream()
                    .filter(f -> esFestivalVigente(f, hoy))
                    .toList();

            modelMap.put("unidadAsignada", unidadVigente);
            modelMap.put("festivalesAsignados", festivalesVigentes);

            Map<String, Object> asignacion = new HashMap<>();

            asignacion.put("unidad", unidadVigente.getNombreComercial());

            asignacion.put("festival", festivalesVigentes.isEmpty()
                    ? "Sin festival vigente"
                    : festivalesVigentes.get(0).getNombre());

            modelMap.put("asignacionActual", asignacion);

        } else {

            modelMap.put("unidadAsignada", null);
            modelMap.put("festivalesAsignados", Collections.emptyList());
            modelMap.put("asignacionActual", asignacionVacia());
        }

        // El historial se carga siempre.
        modelMap.put("historialAsignaciones", obtenerHistorialAsignaciones(staff));

        return modelMap;
    }

    private List<AsignacionStaffDTO> obtenerHistorialAsignaciones(Staff staff) {

        return asignacionStaffRepository
                .findByStaff_IdOrderByFechaInicioDesc(staff.getId())
                .stream()
                .map(asignacion -> new AsignacionStaffDTO(
                        asignacion.getFestival().getNombre(),
                        asignacion.getUnidadVenta().getNombreComercial(),
                        asignacion.getFechaInicio(),
                        asignacion.getFechaFin()
                ))
                .toList();
    }

    // ─── Métodos auxiliares privados ─────────────────────────────────────

    private boolean estaAsignadoAUnidad(UnidadVenta unidad, Staff staff) {

        boolean esResponsable = unidad.getResponsable() != null
                && Objects.equals(unidad.getResponsable().getId(), staff.getId());

        boolean estaEnStaff = unidad.getStaffAsignado() != null
                && unidad.getStaffAsignado().stream()
                        .anyMatch(s -> Objects.equals(s.getId(), staff.getId()));

        return esResponsable || estaEnStaff;
    }

    private boolean tieneFestivalVigente(UnidadVenta unidad, LocalDate hoy) {

        return unidad.getFestivales() != null
                && unidad.getFestivales().stream()
                        .anyMatch(f -> esFestivalVigente(f, hoy));
    }

    private boolean esFestivalVigente(Festival festival, LocalDate hoy) {

        return festival.getFechaInicio() != null
                && festival.getFechaFin() != null
                && !hoy.isBefore(festival.getFechaInicio())
                && !hoy.isAfter(festival.getFechaFin());
    }

    private String calcularAntiguedad(LocalDate fechaIngreso) {

        if (fechaIngreso == null) {
            return "Sin fecha registrada";
        }

        LocalDate hoy = LocalDate.now();

        if (fechaIngreso.isAfter(hoy)) {
            return "Fecha de ingreso inválida";
        }

        Period periodo = Period.between(fechaIngreso, hoy);

        int anios = periodo.getYears();
        int meses = periodo.getMonths();
        int dias = periodo.getDays();

        if (anios > 0) {

            String resultado = anios + (anios == 1 ? " año" : " años");

            if (meses > 0) {
                resultado += " y " + meses + (meses == 1 ? " mes" : " meses");
            }

            return resultado;

        } else if (meses > 0) {

            String resultado = meses + (meses == 1 ? " mes" : " meses");

            if (dias > 0) {
                resultado += " y " + dias + (dias == 1 ? " día" : " días");
            }

            return resultado;

        } else {
            return dias + (dias == 1 ? " día" : " días");
        }
    }

    private String resolverRol(Staff staff) {

        if (staff.getUsuario() == null || staff.getUsuario().getRol() == null) {
            return "Sin rol asignado";
        }

        String nombre = staff.getUsuario().getRol().getNombre()
                .replace("ROLE_", "")
                .trim()
                .toUpperCase();

        return switch (nombre) {
            case "ADMIN" -> "Administrador";
            case "RESPONSABLE" -> "Responsable";
            case "CAJERO" -> "Cajero";
            case "COCINERO" -> "Cocinero";
            case "EMPLEADO" -> "Empleado";
            default -> nombre.isEmpty()
                    ? "Sin rol asignado"
                    : nombre.substring(0, 1) + nombre.substring(1).toLowerCase();
        };
    }

    private Map<String, Object> asignacionVacia() {

        Map<String, Object> asignacion = new HashMap<>();

        asignacion.put("unidad", "Sin unidad con festival vigente");
        asignacion.put("festival", "Sin festival vigente");

        return asignacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionStaffDTO> obtenerHistorialAsignaciones(String email) {

        Staff staff = staffRepository.findByUsuario_Email(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un empleado asociado al usuario " + email));

        return obtenerHistorialAsignaciones(staff);
    }
}