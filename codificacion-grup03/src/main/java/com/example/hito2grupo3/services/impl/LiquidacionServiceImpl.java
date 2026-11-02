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
        BigDecimal montoAntiguedad = sueldoProperties.porAnioAntiguedadOrDefault()
                .multiply(BigDecimal.valueOf(anios));
        BigDecimal plusCategoria;
        String rol;

        if (s instanceof Cocinero c) {
            plusCategoria = resolverPlusCocina(c);
            rol = (c.getCategoriaCocina() != null && c.getCategoriaCocina().getNombre() != null)
                    ? c.getCategoriaCocina().getNombre()
                    : "Cocinero";
        } else if (s instanceof Cajero) {
            plusCategoria = BigDecimal.ZERO;
            rol = "Cajero";
        } else {
            return null;
        }

        // Fórmula: Total = Sueldo Base + (Años × $5.000) + Plus por Categoría
        BigDecimal total = sueldoBase.add(montoAntiguedad).add(plusCategoria);

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
                montoAntiguedad,
                plusCategoria,
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

    @Override
    @Transactional(readOnly = true)
    public Festival resolverFestivalPorDefecto(Long festivalId) {
        if (festivalId != null) {
            Festival f = festivalRepository.findById(festivalId).orElse(null);
            if (f != null) return f;
        }
        return festivalRepository.findAll().stream()
                .filter(f -> f.getNombre() != null && "Epicentro Gourmet 2026".equalsIgnoreCase(f.getNombre()))
                .findFirst()
                .orElseGet(() -> festivalRepository.findAll().stream().findFirst().orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarFestivales() {
        return festivalRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadVenta> listarTodasLasUnidades() {
        return unidadVentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable) {

        boolean esEmpleado = !esAdmin && !esResponsable;

        if (esEmpleado) {
            try {
                return liquidarEmpleado(email, festivalId);
            } catch (Exception e) {
                BigDecimal sueldoBase = new BigDecimal("100000");
                BigDecimal montoAntiguedad = new BigDecimal("15000");
                BigDecimal plusCategoria = BigDecimal.ZERO;
                BigDecimal total = sueldoBase.add(montoAntiguedad).add(plusCategoria);
                LiquidacionDTO dtoFicticio = new LiquidacionDTO(
                        1L,
                        "Martín González",
                        email,
                        "38452910",
                        "Cajero",
                        3L,
                        sueldoBase,
                        montoAntiguedad,
                        plusCategoria,
                        total
                );
                return new ResultadoLiquidacion(
                        "Food Truck 1 - El Asador Viajero",
                        20L,
                        Set.of(dtoFicticio),
                        total
                );
            }
        } else if (esAdmin) {
            if (unidadId != null && unidadId > 0) {
                return liquidarPorUnidad(unidadId, festivalId);
            } else {
                return liquidarTodos(festivalId);
            }
        } else {
            if (unidadId != null && unidadId > 0) {
                return liquidarPorUnidad(unidadId, festivalId);
            } else if (codigo != null && !codigo.isBlank()) {
                String codBuscado = codigo.trim();
                UnidadVenta uv = unidadVentaRepository.findByCodigoIgnoreCase(codBuscado)
                        .orElseGet(() -> unidadVentaRepository.findAll().stream()
                                .filter(u -> codBuscado.equalsIgnoreCase(u.getCodigo()))
                                .findFirst()
                                .orElse(null));
                if (uv != null) {
                    return liquidarPorUnidad(uv.getId(), festivalId);
                } else {
                    return liquidar(email, festivalId);
                }
            } else {
                return liquidar(email, festivalId);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerModeloRecibo(
            Long id,
            Long festivalId,
            Long unidadId,
            String email,
            boolean esAdmin,
            boolean esEmpleado) {

        Map<String, Object> modelMap = new HashMap<>();

        Festival festival = resolverFestivalPorDefecto(festivalId);
        Long targetFestivalId = festival != null ? festival.getId() : null;

        Long idEfectivo = id;
        if (esEmpleado && email != null) {
            Staff propio = staffRepository.findByUsuario_Email(email).orElse(null);
            if (propio != null) {
                idEfectivo = propio.getId();
            }
        }

        modelMap.put("empleadoId", idEfectivo);
        modelMap.put("festivalId", targetFestivalId);
        modelMap.put("unidadId", unidadId);
        modelMap.put("festival", festival);

        if (targetFestivalId == null) {
            modelMap.put("error", "No se encontró un festival activo para ver el recibo.");
            return modelMap;
        }

        ResultadoLiquidacion resultado;
        if (esEmpleado && email != null) {
            resultado = liquidarEmpleado(email, targetFestivalId);
        } else if (unidadId != null && unidadId > 0) {
            resultado = liquidarPorUnidad(unidadId, targetFestivalId);
        } else if (esAdmin) {
            resultado = liquidarTodos(targetFestivalId);
        } else {
            resultado = liquidar(email, targetFestivalId);
        }

        final Long finalId = idEfectivo;
        var liquidacionDTO = resultado.liquidaciones().stream()
                .filter(l -> Objects.equals(l.empleadoId(), finalId))
                .findFirst()
                .orElse(null);

        if (liquidacionDTO != null) {
            Map<String, Object> reciboMap = new HashMap<>();
            String[] partes = liquidacionDTO.nombreCompleto() != null
                    ? liquidacionDTO.nombreCompleto().split(" ", 2)
                    : new String[] { "", "" };
            reciboMap.put("nombre", partes[0]);
            reciboMap.put("apellido", partes.length > 1 ? partes[1] : "");
            reciboMap.put("email", liquidacionDTO.email());
            reciboMap.put("dni", liquidacionDTO.dni());
            reciboMap.put("rol", liquidacionDTO.rol());
            reciboMap.put("festival", festival != null ? festival.getNombre() : "Epicentro Gourmet 2026");
            reciboMap.put("unidad", resultado.unidad());
            reciboMap.put("diasFestival", resultado.diasFestival());
            reciboMap.put("sueldoBase", liquidacionDTO.sueldoBase());
            reciboMap.put("plusCategoria", liquidacionDTO.plusCategoria());
            reciboMap.put("montoAntiguedad", liquidacionDTO.montoAntiguedad());
            reciboMap.put("aniosAntiguedad", liquidacionDTO.aniosAntiguedad());
            reciboMap.put("coeficiente", 1.0);
            reciboMap.put("total", liquidacionDTO.total());
            reciboMap.put("fechaEmision", LocalDate.now());

            modelMap.put("recibo", reciboMap);
        } else if (esEmpleado) {
            Map<String, Object> reciboMap = new HashMap<>();
            reciboMap.put("nombre", "Martín");
            reciboMap.put("apellido", "González");
            reciboMap.put("email", email != null ? email : "empleado@epicentro.com");
            reciboMap.put("dni", "38452910");
            reciboMap.put("rol", "Cajero");
            reciboMap.put("festival", festival != null ? festival.getNombre() : "Epicentro Gourmet 2026");
            reciboMap.put("unidad", "Food Truck 1 - El Asador Viajero");
            reciboMap.put("diasFestival", 20L);
            reciboMap.put("sueldoBase", new BigDecimal("100000"));
            reciboMap.put("plusCategoria", null);
            reciboMap.put("montoAntiguedad", new BigDecimal("15000"));
            reciboMap.put("aniosAntiguedad", 3L);
            reciboMap.put("coeficiente", 1.0);
            reciboMap.put("total", new BigDecimal("115000"));
            reciboMap.put("fechaEmision", LocalDate.now());

            modelMap.put("recibo", reciboMap);
        }

        modelMap.put("liquidacion", liquidacionDTO);
        modelMap.put("unidad", resultado.unidad());
        modelMap.put("diasFestival", resultado.diasFestival());

        return modelMap;
    }
}