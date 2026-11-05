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
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una unidad de venta a cargo del usuario: " + emailResponsable));

        return calcularLiquidacion(unidad, festival, festival.getFechaInicio(), festival.getFechaFin());
    }

        @Override
        @Transactional(readOnly = true)
        public ResultadoLiquidacion liquidarPorUnidadEntreFechas(Long unidadId, Long festivalId,
                                       LocalDate fechaDesde, LocalDate fechaHasta) {
        Festival festival = festivalRepository.findById(festivalId)
            .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));
        UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
            .orElseThrow(() -> new IllegalArgumentException("La unidad de venta seleccionada no existe."));

        LocalDate desde = fechaDesde != null ? fechaDesde : festival.getFechaInicio();
        LocalDate hasta = fechaHasta != null ? fechaHasta : festival.getFechaFin();
        validarPeriodo(festival, desde, hasta);
        return calcularLiquidacion(unidad, festival, desde, hasta);
        }

    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarPorUnidad(Long unidadId, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException("La unidad de venta seleccionada no existe."));

            return calcularLiquidacion(unidad, festival, festival.getFechaInicio(), festival.getFechaFin());
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

        // Solo se admite coincidencia exacta por email del usuario vinculado al Staff.
        // No hay búsqueda por nombre ni "primer staff disponible": si no existe, se rechaza.
        Staff empleado = staffRepository.findByUsuario_Email(emailEmpleado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un empleado asociado al usuario: " + emailEmpleado));

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

    private ResultadoLiquidacion calcularLiquidacion(UnidadVenta unidad, Festival festival,
                                                      LocalDate fechaDesde, LocalDate fechaHasta) {
        long dias = diasDelFestival(fechaDesde, fechaHasta);
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
        
        int diasPorPeriodo = sueldoProperties.diasPorPeriodoOrDefault();
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
            plusCategoria = BigDecimal.ZERO;
            rol = "Empleado";
        }

        BigDecimal sueldo = s.getSueldo() != null ? s.getSueldo() : sueldoBase;
        BigDecimal total = sueldo.add(montoAntiguedad).add(plusCategoria).multiply(factorProporcional);
        BigDecimal sueldoLiquidado = sueldo.multiply(factorProporcional);
        BigDecimal antiguedadLiquidada = montoAntiguedad.multiply(factorProporcional);
        BigDecimal plusLiquidado = plusCategoria.multiply(factorProporcional);

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
                sueldoLiquidado,
                antiguedadLiquidada,
                plusLiquidado,
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
        if (fechaIngreso == null || referencia == null || fechaIngreso.isAfter(referencia)) {
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

    private void validarPeriodo(Festival festival, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El período consultado no es válido.");
        }
        if (desde.isBefore(festival.getFechaInicio()) || hasta.isAfter(festival.getFechaFin())) {
            throw new IllegalArgumentException("El período consultado debe estar dentro del festival.");
        }
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
            return liquidarEmpleado(email, festivalId);
        } else if (esAdmin) {
            if (unidadId != null && unidadId > 0) {
                return liquidarPorUnidad(unidadId, festivalId);
            } else {
                return liquidarTodos(festivalId);
            }
        } else {
            // RESPONSABLE: solo puede liquidar la unidad que tiene a cargo.
            UnidadVenta propia = buscarUnidadDelResponsable(email);

            if (unidadId != null && unidadId > 0 && !Objects.equals(unidadId, propia.getId())) {
                throw new IllegalArgumentException("No tiene permiso para liquidar una unidad que no está a su cargo.");
            }
            if (codigo != null && !codigo.isBlank() && !codigo.trim().equalsIgnoreCase(propia.getCodigo())) {
                throw new IllegalArgumentException("No tiene permiso para liquidar una unidad que no está a su cargo.");
            }
            return liquidarPorUnidad(propia.getId(), festivalId);
        }
    }

    private UnidadVenta buscarUnidadDelResponsable(String emailResponsable) {
        if (emailResponsable == null) {
            throw new IllegalArgumentException("Usuario no autenticado.");
        }
        return unidadVentaRepository.findAll().stream()
                .filter(u -> u.getResponsable() != null
                        && u.getResponsable().getUsuario() != null
                        && emailResponsable.equalsIgnoreCase(u.getResponsable().getUsuario().getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una unidad de venta a cargo del usuario: " + emailResponsable));
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
        if (esEmpleado) {
            resultado = liquidarEmpleado(email, targetFestivalId);
        } else if (esAdmin) {
            resultado = (unidadId != null && unidadId > 0)
                    ? liquidarPorUnidad(unidadId, targetFestivalId)
                    : liquidarTodos(targetFestivalId);
        } else {
            // RESPONSABLE: solo recibos de empleados de su propia unidad.
            UnidadVenta propia = buscarUnidadDelResponsable(email);
            if (unidadId != null && unidadId > 0 && !Objects.equals(unidadId, propia.getId())) {
                throw new IllegalArgumentException("No tiene permiso para ver recibos de una unidad que no está a su cargo.");
            }
            resultado = liquidarPorUnidad(propia.getId(), targetFestivalId);
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
        } else {
            throw new IllegalArgumentException(
                    "No se encontró una liquidación del empleado solicitado dentro de su alcance.");
        }

        modelMap.put("liquidacion", liquidacionDTO);
        modelMap.put("unidad", resultado.unidad());
        modelMap.put("diasFestival", resultado.diasFestival());

        return modelMap;
    }
}