package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionServiceImpl implements ILiquidacionService {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final SueldoProperties sueldoProperties;

    public LiquidacionServiceImpl(FestivalRepository festivalRepository,
                                  UnidadVentaRepository unidadVentaRepository,
                                  StaffRepository staffRepository,
                                  SueldoProperties sueldoProperties) {
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.sueldoProperties = sueldoProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidar(String emailResponsable, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        UnidadVenta unidad = unidadVentaRepository.findAll().stream()
                .filter(u -> u.getResponsable() != null
                        && u.getResponsable().getUsuario() != null
                        && emailResponsable != null
                        && emailResponsable.equals(u.getResponsable().getUsuario().getEmail()))
                .findFirst()
                .orElseGet(() -> unidadVentaRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No hay unidades de venta registradas.")));

        return calcularLiquidacion(unidad, festival);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarPorUnidad(Long unidadId, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException("La unidad de venta seleccionada no existe."));

        return calcularLiquidacion(unidad, festival);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarTodos(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        BigDecimal factorProporcional = calcularFactorProporcional(dias);
        int periodos = calcularPeriodos(dias);

        Set<UnidadVenta> unidades = festival.getUnidadesVentaHabilitadas();
        if (unidades == null || unidades.isEmpty()) {
            unidades = new LinkedHashSet<>(unidadVentaRepository.findAll());
        }

        // Obtener todo el personal sin duplicados respetando id
        Map<Long, Staff> personalMap = new LinkedHashMap<>();
        for (UnidadVenta uv : unidades) {
            if (uv.getResponsable() != null) {
                personalMap.put(uv.getResponsable().getId(), uv.getResponsable());
            }
            if (uv.getStaffAsignado() != null) {
                for (Staff s : uv.getStaffAsignado()) {
                    if (s != null) {
                        personalMap.put(s.getId(), s);
                    }
                }
            }
        }

        Set<LiquidacionDTO> conjuntoLiquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;
        BigDecimal sueldoBase = sueldoProperties.baseOrDefault();

        for (Staff s : personalMap.values()) {
            LiquidacionDTO dto = armarLiquidacionEmpleado(s, festival, sueldoBase, factorProporcional, periodos, null);
            if (dto != null) {
                conjuntoLiquidaciones.add(dto);
                totalGeneral = totalGeneral.add(dto.total());
            }
        }

        return new ResultadoLiquidacion("Todas las unidades", dias, conjuntoLiquidaciones, totalGeneral);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarEmpleado(String emailEmpleado, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        Staff empleado = staffRepository.findByUsuario_Email(emailEmpleado)
                .orElse(null);

        // Si no se encontró por email en StaffRepository, buscar por email en responsable o asignados
        if (empleado == null) {
            empleado = unidadVentaRepository.findAll().stream()
                    .flatMap(uv -> {
                        List<Staff> lista = new ArrayList<>();
                        if (uv.getResponsable() != null) lista.add(uv.getResponsable());
                        if (uv.getStaffAsignado() != null) lista.addAll(uv.getStaffAsignado());
                        return lista.stream();
                    })
                    .filter(s -> s.getUsuario() != null && emailEmpleado.equalsIgnoreCase(s.getUsuario().getEmail()))
                    .findFirst()
                    .orElse(null);
        }

        // Buscar si el username/email coincide con el nombre del staff
        if (empleado == null) {
            String parte = emailEmpleado.contains("@") ? emailEmpleado.substring(0, emailEmpleado.indexOf("@")) : emailEmpleado;
            empleado = staffRepository.findAll().stream()
                    .filter(s -> s.getNombre() != null && parte.toLowerCase().contains(s.getNombre().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }

        // Fallback al primer staff disponible tal como hace EmpleadoController si no está vinculado
        if (empleado == null) {
            empleado = staffRepository.findAll().stream().findFirst().orElse(null);
        }

        if (empleado == null) {
            throw new IllegalArgumentException("No se encontró ningún empleado disponible para el usuario: " + emailEmpleado);
        }

        // Buscar unidad a la que pertenece
        final Long empId = empleado.getId();
        UnidadVenta unidad = unidadVentaRepository.findAll().stream()
                .filter(u -> (u.getResponsable() != null && Objects.equals(u.getResponsable().getId(), empId))
                        || (u.getStaffAsignado() != null && u.getStaffAsignado().stream()
                                .anyMatch(s -> Objects.equals(s.getId(), empId))))
                .findFirst()
                .orElse(null);

        String nombreUnidad = unidad != null ? unidad.getNombreComercial() : "Unidad asignada";

        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        BigDecimal factorProporcional = calcularFactorProporcional(dias);
        int periodos = calcularPeriodos(dias);
        BigDecimal sueldoBase = sueldoProperties.baseOrDefault();

        LiquidacionDTO dto = armarLiquidacionEmpleado(empleado, festival, sueldoBase, factorProporcional, periodos, emailEmpleado);
        if (dto == null) {
            throw new IllegalArgumentException("El empleado no cuenta con un tipo de puesto liquidable (Cajero o Cocinero).");
        }

        Set<LiquidacionDTO> conjunto = new LinkedHashSet<>();
        conjunto.add(dto);

        return new ResultadoLiquidacion(nombreUnidad, dias, conjunto, dto.total());
    }

    private ResultadoLiquidacion calcularLiquidacion(UnidadVenta unidad, Festival festival) {
        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        BigDecimal factorProporcional = calcularFactorProporcional(dias);
        int periodos = calcularPeriodos(dias);

        Set<LiquidacionDTO> conjuntoLiquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        Map<Long, Staff> personalMap = new LinkedHashMap<>();
        if (unidad.getResponsable() != null) {
            personalMap.put(unidad.getResponsable().getId(), unidad.getResponsable());
        }
        if (unidad.getStaffAsignado() != null) {
            for (Staff s : unidad.getStaffAsignado()) {
                if (s != null) {
                    personalMap.put(s.getId(), s);
                }
            }
        }

        BigDecimal sueldoBase = sueldoProperties.baseOrDefault();

        for (Staff s : personalMap.values()) {
            LiquidacionDTO dto = armarLiquidacionEmpleado(s, festival, sueldoBase, factorProporcional, periodos, null);
            if (dto != null) {
                totalGeneral = totalGeneral.add(dto.total());
                conjuntoLiquidaciones.add(dto);
            }
        }

        return new ResultadoLiquidacion(unidad.getNombreComercial(), dias, conjuntoLiquidaciones, totalGeneral);
    }

    private int calcularPeriodos(long dias) {
        
        int diasPorPeriodo = 30; // o usa sueldoProperties.diasPorPeriodoOrDefault() si lo tienes configurado
        if (dias <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) dias / diasPorPeriodo);
    }

  

    private BigDecimal calcularFactorProporcional(long dias) {
        int periodos = calcularPeriodos(dias);
        return BigDecimal.valueOf(periodos).multiply(sueldoProperties.porcentajePorPeriodoOrDefault());
    }

    private LiquidacionDTO armarLiquidacionEmpleado(Staff s, Festival festival, BigDecimal sueldoBase,
                                                   BigDecimal factorProporcional, int periodos, String emailOverride) {
        long anios = aniosAntiguedad(s.getFechaIngreso(), festival.getFechaInicio());
        BigDecimal adicional;
        String rol;

        if (s instanceof Cocinero c) {
            adicional = resolverPlusCocina(c);
            rol = (c.getCategoriaCocina() != null && c.getCategoriaCocina().getNombre() != null)
                    ? c.getCategoriaCocina().getNombre()
                    : "Cocinero";
        } else if (s instanceof Cajero) {
            BigDecimal plusAntig = sueldoProperties.porAnioAntiguedadOrDefault()
                    .multiply(BigDecimal.valueOf(anios));
            adicional = plusAntig;
            rol = "Cajero";
        } else {
            return null;
        }

        BigDecimal baseCalculo = sueldoBase.add(adicional);
        BigDecimal total = baseCalculo.multiply(factorProporcional);

        String email = (s.getUsuario() != null && s.getUsuario().getEmail() != null)
                ? s.getUsuario().getEmail()
                : (emailOverride != null ? emailOverride : "");

        return new LiquidacionDTO(
                s.getId(),
                s.getNombre() + " " + s.getApellido(),
                email,
                s.getDni(),
                rol,
                anios,
                sueldoBase,
                adicional,
                periodos,
                total
        );
    }

    private BigDecimal resolverPlusCocina(Cocinero c) {
        if (c.getCategoriaCocina() == null) {
            return sueldoProperties.plusCocineroOrDefault();
        }

        String nombreCat = c.getCategoriaCocina().getNombre() != null
                ? c.getCategoriaCocina().getNombre().toLowerCase().trim()
                : "";

        if (nombreCat.contains("ayudante")) {
            return sueldoProperties.plusCocineroAyudanteOrDefault();
        } else if (nombreCat.contains("lavaplatos") || nombreCat.contains("bachero")) {
            return sueldoProperties.plusLavaplatosOrDefault();
        } else if (nombreCat.contains("cocinero")) {
            return sueldoProperties.plusCocineroOrDefault();
        } else if (c.getCategoriaCocina().getPlusFijo() != null
                && c.getCategoriaCocina().getPlusFijo().compareTo(BigDecimal.ZERO) > 0) {
            return c.getCategoriaCocina().getPlusFijo();
        }

        return sueldoProperties.plusCocineroOrDefault();
    }

    @Override
    public long diasDelFestival(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El período del festival no es válido.");
        }
        return ChronoUnit.DAYS.between(inicio, fin) + 1;
    }

    @Override
    public long aniosAntiguedad(LocalDate fechaIngreso, LocalDate referencia) {
        if (fechaIngreso == null || fechaIngreso.isAfter(referencia)) {
            return 0;
        }
        return ChronoUnit.YEARS.between(fechaIngreso, referencia);
    }
}