package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.LiquidacionHaberesRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionServiceImpl implements ILiquidacionService {

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final StaffRepository staffRepository;
    private final LiquidacionHaberesRepository liquidacionHaberesRepository;
    private final SueldoProperties sueldoProperties;

    public LiquidacionServiceImpl(
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            StaffRepository staffRepository,
            LiquidacionHaberesRepository liquidacionHaberesRepository,
            SueldoProperties sueldoProperties) {

        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.staffRepository = staffRepository;
        this.liquidacionHaberesRepository = liquidacionHaberesRepository;
        this.sueldoProperties = sueldoProperties;
    }

    /**
     * Obtiene la vista previa del personal de la unidad a cargo del responsable.
     * No persiste liquidaciones.
     */
    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidar(String emailResponsable, Long festivalId) {

        Festival festival = buscarFestival(festivalId);
        UnidadVenta unidad = buscarUnidadDelResponsable(emailResponsable);

        return calcularLiquidacion(unidad, festival, festival.getFechaInicio(), festival.getFechaFin());
    }

    /**
     * Obtiene la vista previa del personal de una unidad determinada.
     * No persiste liquidaciones.
     */
    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarPorUnidad(Long unidadId, Long festivalId) {

        Festival festival = buscarFestival(festivalId);

        UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La unidad de venta seleccionada no existe."));

            return calcularLiquidacion(unidad, festival, festival.getFechaInicio(), festival.getFechaFin());
    }

    /**
     * Calcula la vista previa del personal de una unidad sin guardar datos.
     * Genera una fila por empleado y período mensual cuando el festival supera
     * los 30 días; para períodos de hasta 30 días genera una sola fila.
     */
    private ResultadoLiquidacion calcularLiquidacion(
            UnidadVenta unidad,
            Festival festival) {

        long dias = diasDelFestival(
                festival.getFechaInicio(),
                festival.getFechaFin());

        Set<LiquidacionDTO> liquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        Map<Long, Staff> personalDeUnidad = obtenerPersonalDeUnidad(unidad);

        for (Staff empleado : personalDeUnidad.values()) {
            List<LiquidacionDTO> mensuales = armarLiquidacionesMensuales(
                    empleado,
                    unidad,
                    festival,
                    null);

            for (LiquidacionDTO dto : mensuales) {
                liquidaciones.add(dto);
                totalGeneral = totalGeneral.add(dto.total());
            }
        }

        return new ResultadoLiquidacion(
                unidad.getNombreComercial(),
                dias,
                liquidaciones,
                totalGeneral);
    }

    /**
     * Vista previa de todas las unidades habilitadas para el festival.
     * Si una persona trabaja en más de una unidad, se genera una liquidación
     * independiente para cada combinación de empleado y unidad.
     */
    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarTodos(Long festivalId) {

        Festival festival = buscarFestival(festivalId);
        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());

        Set<UnidadVenta> unidades = festival.getUnidadesVentaHabilitadas();
        if (unidades == null || unidades.isEmpty()) {
            unidades = new LinkedHashSet<>(unidadVentaRepository.findAll());
        }

        Set<LiquidacionDTO> liquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (UnidadVenta unidad : unidades) {
            if (unidad == null) {
                continue;
            }

            Map<Long, Staff> personalDeUnidad = obtenerPersonalDeUnidad(unidad);

            for (Staff empleado : personalDeUnidad.values()) {
                List<LiquidacionDTO> mensuales = armarLiquidacionesMensuales(
                        empleado, unidad, festival, null);

                for (LiquidacionDTO dto : mensuales) {
                    liquidaciones.add(dto);
                    totalGeneral = totalGeneral.add(dto.total());
                }
            }
        }

        return new ResultadoLiquidacion(
                "Todas las unidades", dias, liquidaciones, totalGeneral);
    }

    /**
     * Para los empleados, devuelve exclusivamente los registros históricos
     * que ya fueron guardados para el festival seleccionado.
     */
    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidarEmpleado(String emailEmpleado, Long festivalId) {

        Festival festival = buscarFestival(festivalId);

        if (emailEmpleado == null || emailEmpleado.isBlank()) {
            throw new IllegalArgumentException("No se recibió un usuario de empleado válido.");
        }

        Staff empleado = staffRepository.findByUsuario_Email(emailEmpleado).orElse(null);

        // Alternativa para instalaciones donde el nombre de usuario no sea el email.
        if (empleado == null) {
            String parteUsuario = emailEmpleado.contains("@")
                    ? emailEmpleado.substring(0, emailEmpleado.indexOf("@"))
                    : emailEmpleado;

            empleado = staffRepository.findAll().stream()
                    .filter(s -> s.getNombre() != null
                            && parteUsuario.toLowerCase().contains(
                                    s.getNombre().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }
        final String emailLimpio = emailEmpleado.trim();

        if (empleado == null) {
            throw new IllegalArgumentException(
                    "No se encontró un empleado asociado al usuario: " + emailEmpleado);
        }

        List<LiquidacionHaberes> registros =
                liquidacionHaberesRepository.buscarPorEmpleadoYFestival(
                        empleado.getId(), festival.getId());

        Set<LiquidacionDTO> liquidaciones = new LinkedHashSet<>();
        Set<String> nombresUnidades = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        String nombreCompleto =
                (empleado.getNombre() != null ? empleado.getNombre() : "") + " "
                + (empleado.getApellido() != null ? empleado.getApellido() : "");

        String email = empleado.getUsuario() != null
                && empleado.getUsuario().getEmail() != null
                ? empleado.getUsuario().getEmail()
                : emailEmpleado;

        for (LiquidacionHaberes registro : registros) {
            String rol;
            long anios = 0;

            if (empleado instanceof Cocinero cocinero) {
                rol = cocinero.getCategoriaCocina() != null
                        && cocinero.getCategoriaCocina().getNombre() != null
                        ? cocinero.getCategoriaCocina().getNombre()
                        : "Cocinero";
            } else if (empleado instanceof Cajero) {
                rol = "Cajero";
                anios = aniosAntiguedad(
                        empleado.getFechaIngreso(), registro.getPeriodoInicio());
            } else {
                continue;
            }

            UnidadVenta unidadRegistro = registro.getUnidadVenta();

            LiquidacionDTO dto = new LiquidacionDTO(
                    empleado.getId(),
                    nombreCompleto.trim(),
                    email,
                    empleado.getDni(),
                    rol,
                    anios,
                    registro.getSueldoBase(),
                    registro.getMontoAntiguedad(),
                    registro.getPlusCategoria(),
                    registro.getMontoCalculado(),
                    unidadRegistro != null ? unidadRegistro.getId() : null,
                    unidadRegistro != null ? unidadRegistro.getNombreComercial() : null,
                     registro.getPeriodoInicio(),
                    registro.getPeriodoFin(),
                    registro.getDiasTrabajados() != null
                            ? registro.getDiasTrabajados() : 0,
                    registro.getFactorAplicado(),
                    registro.getId());

            liquidaciones.add(dto);
            totalGeneral = totalGeneral.add(registro.getMontoCalculado());

            if (unidadRegistro != null && unidadRegistro.getNombreComercial() != null) {
                nombresUnidades.add(unidadRegistro.getNombreComercial());
            }
        }

        String unidad = nombresUnidades.isEmpty()
                ? "Sin liquidaciones registradas"
                : String.join(", ", nombresUnidades);

        long diasFestival = diasDelFestival(
                festival.getFechaInicio(), festival.getFechaFin());

        return new ResultadoLiquidacion(
                unidad, diasFestival, liquidaciones, totalGeneral);
    }

    /**
     * Obtiene el personal de una unidad sin repetir al responsable si también
     * aparece en la colección de staff asignado.
     */
    private Map<Long, Staff> obtenerPersonalDeUnidad(UnidadVenta unidad) {

        Map<Long, Staff> personal = new LinkedHashMap<>();

        if (unidad.getResponsable() != null) {
            Staff responsable = unidad.getResponsable();
            personal.put(responsable.getId(), responsable);
        }

        if (unidad.getStaffAsignado() != null) {
            for (Staff empleado : unidad.getStaffAsignado()) {
                if (empleado != null) {
                    personal.putIfAbsent(empleado.getId(), empleado);
                }
            }
        }

        return personal;
    }

    /**
     * Calcula las liquidaciones mensuales de una persona en una unidad.
     * Primero aplica al festival completo la fórmula de la cátedra y luego
     * distribuye el importe entre los períodos de mes calendario. El último
     * período absorbe los centavos de redondeo para conservar el total exacto.
     */
    private List<LiquidacionDTO> armarLiquidacionesMensuales(
            Staff empleado,
            UnidadVenta unidad,
            Festival festival,
            String emailOverride) {

        LocalDate inicioFestival = festival.getFechaInicio();
        LocalDate finFestival = festival.getFechaFin();
        long diasTotales = diasDelFestival(inicioFestival, finFestival);

        BigDecimal factorTotal = calcularFactorProporcional(diasTotales);
        BigDecimal sueldoBase = sueldoProperties.baseOrDefault();

        LiquidacionDTO liquidacionCompleta = armarLiquidacionEmpleado(
                empleado, festival, sueldoBase, factorTotal, emailOverride);

        if (liquidacionCompleta == null) {
            return List.of();
        }

        // Hasta 30 días se conserva una única liquidación por el período completo.
        // La separación mensual se aplica cuando el trabajo supera los 30 días.
        if (diasTotales <= 30) {
            LiquidacionDTO unica = new LiquidacionDTO(
                    liquidacionCompleta.empleadoId(),
                    liquidacionCompleta.nombreCompleto(),
                    liquidacionCompleta.email(),
                    liquidacionCompleta.dni(),
                    liquidacionCompleta.rol(),
                    liquidacionCompleta.aniosAntiguedad(),
                    liquidacionCompleta.sueldoBase(),
                    liquidacionCompleta.montoAntiguedad(),
                    liquidacionCompleta.plusCategoria(),
                    liquidacionCompleta.total(),
                    unidad.getId(),
                    unidad.getNombreComercial(),
                    inicioFestival,
                    finFestival,
                    diasTotales,
                    factorTotal);
            return List.of(unica);
        }

        BigDecimal baseLiquidable = liquidacionCompleta.sueldoBase()
                .add(liquidacionCompleta.montoAntiguedad())
                .add(liquidacionCompleta.plusCategoria());

        List<LiquidacionDTO> resultado = new ArrayList<>();
        LocalDate cursor = inicioFestival;
        BigDecimal factorAcumulado = BigDecimal.ZERO;
        BigDecimal totalAcumulado = BigDecimal.ZERO;

        while (!cursor.isAfter(finFestival)) {
            LocalDate finPeriodo = cursor.withDayOfMonth(cursor.lengthOfMonth());
            if (finPeriodo.isAfter(finFestival)) {
                finPeriodo = finFestival;
            }

            long diasPeriodo = diasDelFestival(cursor, finPeriodo);
            boolean esUltimoPeriodo = finPeriodo.equals(finFestival);

            BigDecimal factorPeriodo;
            if (esUltimoPeriodo) {
                factorPeriodo = factorTotal.subtract(factorAcumulado)
                        .setScale(4, RoundingMode.HALF_UP);
            } else {
                factorPeriodo = factorTotal
                        .multiply(BigDecimal.valueOf(diasPeriodo))
                        .divide(BigDecimal.valueOf(diasTotales), 4, RoundingMode.HALF_UP);
            }

            BigDecimal totalPeriodo;
            if (esUltimoPeriodo) {
                totalPeriodo = liquidacionCompleta.total()
                        .subtract(totalAcumulado)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                totalPeriodo = baseLiquidable
                        .multiply(factorPeriodo)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            LiquidacionDTO dto = new LiquidacionDTO(
                    liquidacionCompleta.empleadoId(),
                    liquidacionCompleta.nombreCompleto(),
                    liquidacionCompleta.email(),
                    liquidacionCompleta.dni(),
                    liquidacionCompleta.rol(),
                    liquidacionCompleta.aniosAntiguedad(),
                    liquidacionCompleta.sueldoBase(),
                    liquidacionCompleta.montoAntiguedad(),
                    liquidacionCompleta.plusCategoria(),
                    totalPeriodo,
                    unidad.getId(),
                    unidad.getNombreComercial(),
                    cursor,
                    finPeriodo,
                    diasPeriodo,
                    factorPeriodo);

            resultado.add(dto);
            factorAcumulado = factorAcumulado.add(factorPeriodo);
            totalAcumulado = totalAcumulado.add(totalPeriodo);
            cursor = finPeriodo.plusDays(1);
        }

        return resultado;
    }

    /**
     * Persiste una liquidación mensual una sola vez por empleado, unidad,
     * festival y período calendario.
     */
    private void guardarLiquidacion(
            Staff empleado,
            UnidadVenta unidad,
            Festival festival,
            LiquidacionDTO dto) {

        LocalDate periodoInicio = dto.periodoInicio();
        LocalDate periodoFin = dto.periodoFin();

        if (periodoInicio == null || periodoFin == null) {
            throw new IllegalArgumentException(
                    "La liquidación no tiene un período mensual válido.");
        }

        boolean yaExiste = liquidacionHaberesRepository.existeLiquidacionParaPeriodo(
                empleado.getId(), unidad.getId(), festival.getId(),
                periodoInicio, periodoFin);

        if (yaExiste) {
            return;
        }

        LiquidacionHaberes liquidacion = new LiquidacionHaberes();
        liquidacion.setStaff(empleado);
        liquidacion.setUnidadVenta(unidad);
        liquidacion.setFestival(festival);
        liquidacion.setPeriodoInicio(periodoInicio);
        liquidacion.setPeriodoFin(periodoFin);
        liquidacion.setDiasTrabajados(Math.toIntExact(dto.diasTrabajados()));
        liquidacion.setFechaLiquidacion(LocalDate.now());
        liquidacion.setMontoCalculado(dto.total());
        liquidacion.setSueldoBase(dto.sueldoBase());
        liquidacion.setMontoAntiguedad(dto.montoAntiguedad());
        liquidacion.setPlusCategoria(dto.plusCategoria());
        liquidacion.setFactorAplicado(dto.factorAplicado());

        liquidacionHaberesRepository.save(liquidacion);
    }

    /**
     * Hasta 15 días: 50%.
     * De 16 a 30 días: 100%.
     * Más de 30 días: (int(días / 15) + 1) * 50%.
     * Se conserva exactamente la fórmula indicada por la cátedra.
     */
    private BigDecimal calcularFactorProporcional(long dias) {

        if (dias <= 0) {
            throw new IllegalArgumentException(
                    "La duración del festival debe ser mayor a cero días.");
        }

        BigDecimal cincuentaPorCiento =
                sueldoProperties.porcentajePorPeriodoOrDefault();

        if (dias <= 15) {
            return cincuentaPorCiento;
        }

        if (dias <= 30) {
            return cincuentaPorCiento.multiply(BigDecimal.valueOf(2));
        }

        long intervalos = (dias / 15) + 1;
        return cincuentaPorCiento.multiply(BigDecimal.valueOf(intervalos));
    }

    /**
     * Calcula el haber total de un empleado según su tipo de puesto y la
     * duración completa del festival. Este método no persiste información.
     */
    private LiquidacionDTO armarLiquidacionEmpleado(
            Staff staff,
            Festival festival,
            BigDecimal sueldoBase,
            BigDecimal factorProporcional,
            String emailOverride) {

        long anios = 0;
        BigDecimal montoAntiguedad = BigDecimal.ZERO;
        BigDecimal plusCategoria = BigDecimal.ZERO;
        BigDecimal baseLiquidable;
        String rol;

        if (staff instanceof Cocinero cocinero) {
            plusCategoria = resolverPlusCocina(cocinero);
            rol = cocinero.getCategoriaCocina() != null
                    && cocinero.getCategoriaCocina().getNombre() != null
                    ? cocinero.getCategoriaCocina().getNombre()
                    : "Cocinero";
            baseLiquidable = sueldoBase.add(plusCategoria);

        } else if (staff instanceof Cajero) {
            anios = aniosAntiguedad(
                    staff.getFechaIngreso(), festival.getFechaInicio());
            montoAntiguedad = sueldoProperties.porAnioAntiguedadOrDefault()
                    .multiply(BigDecimal.valueOf(anios));
            rol = "Cajero";
            baseLiquidable = sueldoBase.add(montoAntiguedad);

        } else {
            // Personal genérico (Staff / Responsable / Empleado que no es Cocinero ni Cajero)
            plusCategoria = BigDecimal.ZERO;
            montoAntiguedad = sueldoProperties.porAnioAntiguedadOrDefault()
                    .multiply(BigDecimal.valueOf(anios));
            if (s.getUsuario() != null && s.getUsuario().getRol() != null && s.getUsuario().getRol().getNombre() != null) {
                String nomRol = s.getUsuario().getRol().getNombre().replace("ROLE_", "").trim();
                rol = switch (nomRol.toUpperCase(Locale.ROOT)) {
                    case "RESPONSABLE" -> "Responsable";
                    case "ADMIN" -> "Administrador";
                    case "CAJERO" -> "Cajero";
                    case "COCINERO" -> "Cocinero";
                    default -> "Empleado";
                };
            } else {
                rol = "Empleado";
            }
        }

        BigDecimal total = baseLiquidable
                .multiply(factorProporcional)
                .setScale(2, RoundingMode.HALF_UP);

        String email = staff.getUsuario() != null
                && staff.getUsuario().getEmail() != null
                ? staff.getUsuario().getEmail()
                : (emailOverride != null ? emailOverride : "");

        String nombreCompleto = (staff.getNombre() != null ? staff.getNombre() : "")
                + " " + (staff.getApellido() != null ? staff.getApellido() : "");

        return new LiquidacionDTO(
                staff.getId(), nombreCompleto.trim(), email, staff.getDni(),
                rol, anios, sueldoBase, montoAntiguedad, plusCategoria, total);
    }

    private BigDecimal resolverPlusCocina(Cocinero cocinero) {

        if (cocinero.getCategoriaCocina() == null) {
            return sueldoProperties.plusCocineroOrDefault();
        }

        String nombreCategoria = cocinero.getCategoriaCocina().getNombre() != null
                ? cocinero.getCategoriaCocina().getNombre().toLowerCase().trim()
                : "";

        if (nombreCategoria.contains("ayudante")) {
            return sueldoProperties.plusCocineroAyudanteOrDefault();
        }

        if (nombreCategoria.contains("lavaplatos")
                || nombreCategoria.contains("bachero")) {
            return sueldoProperties.plusLavaplatosOrDefault();
        }

        if (nombreCategoria.contains("cocinero")) {
            return sueldoProperties.plusCocineroOrDefault();
        }

        if (cocinero.getCategoriaCocina().getPlusFijo() != null
                && cocinero.getCategoriaCocina().getPlusFijo()
                        .compareTo(BigDecimal.ZERO) > 0) {
            return cocinero.getCategoriaCocina().getPlusFijo();
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
            Festival festival = festivalRepository.findById(festivalId).orElse(null);
            if (festival != null) {
                return festival;
            }
        }

        List<Festival> festivales = festivalRepository.findAll();
        return festivales.stream()
                .filter(f -> f.getNombre() != null
                        && "Epicentro Gourmet 2026".equalsIgnoreCase(f.getNombre()))
                .findFirst()
                .orElseGet(() -> festivales.stream().findFirst().orElse(null));
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
        }

        if (esAdmin) {
            if (unidadId != null && unidadId > 0) {
                return liquidarPorUnidad(unidadId, festivalId);
            }
            return liquidarTodos(festivalId);
        }

        UnidadVenta unidadResponsable = buscarUnidadDelResponsable(email);

        if (unidadId != null && unidadId > 0
                && !Objects.equals(unidadResponsable.getId(), unidadId)) {
            throw new IllegalArgumentException(
                    "No tenés permiso para consultar otra unidad de venta.");
        }

        if (codigo != null && !codigo.isBlank()
                && (unidadResponsable.getCodigo() == null
                || !codigo.trim().equalsIgnoreCase(unidadResponsable.getCodigo()))) {
            throw new IllegalArgumentException(
                    "El código indicado no corresponde a tu unidad de venta.");
        }

        return liquidarPorUnidad(unidadResponsable.getId(), festivalId);
    }
    @Override
    @Transactional(readOnly = true)
    public ResultadoLiquidacion consultarLiquidacionesRegistradasSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable) {

        if (!esAdmin && !esResponsable) {
            throw new IllegalArgumentException(
                    "No tenés permiso para consultar estas liquidaciones.");
        }

        Festival festival = buscarFestival(festivalId);

        List<LiquidacionHaberes> registros;
        String nombreUnidad;

        if (esAdmin) {

            if (unidadId != null && unidadId > 0) {

                UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "La unidad de venta seleccionada no existe."));

                registros = liquidacionHaberesRepository.buscarPorFestivalYUnidad(
                        festivalId, unidad.getId());

                nombreUnidad = unidad.getNombreComercial();

            } else {

                registros = liquidacionHaberesRepository.buscarPorFestival(
                        festivalId);

                nombreUnidad = "Todas las unidades";
            }

        } else {

            UnidadVenta unidad = buscarUnidadDelResponsable(email);

            if (unidadId != null && unidadId > 0
                    && !Objects.equals(unidad.getId(), unidadId)) {
                throw new IllegalArgumentException(
                        "No tenés permiso para consultar otra unidad de venta.");
            }

            if (codigo != null && !codigo.isBlank()
                    && (unidad.getCodigo() == null
                    || !codigo.trim().equalsIgnoreCase(unidad.getCodigo()))) {
                throw new IllegalArgumentException(
                        "El código indicado no corresponde a tu unidad de venta.");
            }

            registros = liquidacionHaberesRepository.buscarPorFestivalYUnidad(
                    festivalId, unidad.getId());

            nombreUnidad = unidad.getNombreComercial();
        }

        Set<LiquidacionDTO> liquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (LiquidacionHaberes registro : registros) {

            Staff empleado = registro.getStaff();

            if (empleado == null) {
                continue;
            }

            String rol;
            long anios = 0;

            if (empleado instanceof Cocinero cocinero) {

                rol = cocinero.getCategoriaCocina() != null
                        && cocinero.getCategoriaCocina().getNombre() != null
                        ? cocinero.getCategoriaCocina().getNombre()
                        : "Cocinero";

            } else if (empleado instanceof Cajero) {

                rol = "Cajero";

                anios = aniosAntiguedad(
                        empleado.getFechaIngreso(),
                        registro.getPeriodoInicio());

            } else {
                rol = "Empleado";
            }

            UnidadVenta unidadRegistro = registro.getUnidadVenta();

            String nombreCompleto =
                    (empleado.getNombre() != null ? empleado.getNombre() : "")
                    + " "
                    + (empleado.getApellido() != null ? empleado.getApellido() : "");

            String emailEmpleado = empleado.getUsuario() != null
                    ? empleado.getUsuario().getEmail()
                    : "";

            BigDecimal sueldoBase = registro.getSueldoBase() != null
                    ? registro.getSueldoBase() : BigDecimal.ZERO;

            BigDecimal montoAntiguedad = registro.getMontoAntiguedad() != null
                    ? registro.getMontoAntiguedad() : BigDecimal.ZERO;

            BigDecimal plusCategoria = registro.getPlusCategoria() != null
                    ? registro.getPlusCategoria() : BigDecimal.ZERO;

            BigDecimal total = registro.getMontoCalculado() != null
                    ? registro.getMontoCalculado() : BigDecimal.ZERO;

            BigDecimal factor = registro.getFactorAplicado() != null
                    ? registro.getFactorAplicado() : BigDecimal.ZERO;

            LiquidacionDTO dto = new LiquidacionDTO(
                    empleado.getId(),
                    nombreCompleto.trim(),
                    emailEmpleado,
                    empleado.getDni(),
                    rol,
                    anios,
                    sueldoBase,
                    montoAntiguedad,
                    plusCategoria,
                    total,
                    unidadRegistro != null ? unidadRegistro.getId() : null,
                    unidadRegistro != null
                            ? unidadRegistro.getNombreComercial() : null,
                    registro.getPeriodoInicio(),
                    registro.getPeriodoFin(),
                    registro.getDiasTrabajados() != null
                            ? registro.getDiasTrabajados() : 0,
                    factor,
                    registro.getId()
            );

            liquidaciones.add(dto);
            totalGeneral = totalGeneral.add(total);
        }

        long diasFestival = diasDelFestival(
                festival.getFechaInicio(),
                festival.getFechaFin());

        return new ResultadoLiquidacion(
                nombreUnidad,
                diasFestival,
                liquidaciones,
                totalGeneral
        );
    }
    /**
     * Persiste explícitamente los registros mensuales de la vista previa.
     * Requiere ADMIN o RESPONSABLE; consultar la pantalla no guarda datos.
     */
    @Override
    @Transactional
    public ResultadoLiquidacion registrarLiquidacionesSegunRol(
            String email,
            Long festivalId,
            Long unidadId,
            String codigo,
            boolean esAdmin,
            boolean esResponsable) {

        if (!esAdmin && !esResponsable) {
            throw new IllegalArgumentException(
                    "No tenés permiso para registrar liquidaciones.");
        }

        ResultadoLiquidacion resultado = liquidarSegunRol(
                email, festivalId, unidadId, codigo, esAdmin, esResponsable);
        Festival festival = buscarFestival(festivalId);

        Set<UnidadVenta> unidadesParaRegistrar = new LinkedHashSet<>();

        if (esAdmin && unidadId != null && unidadId > 0) {
            UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La unidad de venta seleccionada no existe."));
            unidadesParaRegistrar.add(unidad);
        } else if (esAdmin) {
            Set<UnidadVenta> unidadesFestival = festival.getUnidadesVentaHabilitadas();
            if (unidadesFestival == null || unidadesFestival.isEmpty()) {
                unidadesParaRegistrar.addAll(unidadVentaRepository.findAll());
            } else {
                unidadesParaRegistrar.addAll(unidadesFestival);
            }
        } else {
            unidadesParaRegistrar.add(buscarUnidadDelResponsable(email));
        }

        Map<Long, Staff> personalPorId = new LinkedHashMap<>();
        Map<Long, UnidadVenta> unidadesPorId = new LinkedHashMap<>();

        for (UnidadVenta unidad : unidadesParaRegistrar) {
            if (unidad == null) {
                continue;
            }

            unidadesPorId.put(unidad.getId(), unidad);
            personalPorId.putAll(obtenerPersonalDeUnidad(unidad));
        }

        for (LiquidacionDTO dto : resultado.liquidaciones()) {
            Staff empleado = personalPorId.get(dto.empleadoId());
            UnidadVenta unidad = dto.unidadId() != null
                    ? unidadesPorId.get(dto.unidadId())
                    : null;

            if (empleado == null || unidad == null) {
                throw new IllegalStateException(
                        "No se pudo determinar el empleado o la unidad para registrar "
                        + "la liquidación del empleado " + dto.empleadoId() + ".");
            }

            guardarLiquidacion(empleado, unidad, festival, dto);
        }

        return resultado;
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
            Long liquidacionId,
            String email,
            boolean esAdmin,
            boolean esResponsable) {

        Map<String, Object> modelMap = new LinkedHashMap<>();

        if (liquidacionId == null) {
            throw new IllegalArgumentException(
                    "No se indicó una liquidación válida.");
        }

        // Buscar la liquidación individual registrada.
        LiquidacionHaberes registro = liquidacionHaberesRepository
                .findById(liquidacionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la liquidación solicitada."));

        Staff empleado = registro.getStaff();
        UnidadVenta unidad = registro.getUnidadVenta();
        Festival festival = registro.getFestival();

        if (empleado == null || unidad == null || festival == null) {
            throw new IllegalStateException(
                    "La liquidación tiene datos incompletos.");
        }

        // Verificar los permisos del usuario autenticado.
        if (!esAdmin) {

            if (esResponsable) {

                UnidadVenta unidadResponsable =
                        buscarUnidadDelResponsable(email);

                if (!Objects.equals(
                        unidadResponsable.getId(),
                        unidad.getId())) {

                    throw new IllegalArgumentException(
                            "No tenés permiso para consultar liquidaciones de otra unidad.");
                }

            } else {

                String emailEmpleado = empleado.getUsuario() != null
                        ? empleado.getUsuario().getEmail()
                        : null;

                if (email == null || emailEmpleado == null
                        || !email.equalsIgnoreCase(emailEmpleado)) {

                    throw new IllegalArgumentException(
                            "No tenés permiso para consultar este recibo.");
                }
            }
        }

        // Determinar el puesto y la antigüedad del empleado.
        String rol;
        long aniosAntiguedad = 0;

        if (empleado instanceof Cocinero cocinero) {

            rol = cocinero.getCategoriaCocina() != null
                    && cocinero.getCategoriaCocina().getNombre() != null
                    ? cocinero.getCategoriaCocina().getNombre()
                    : "Cocinero";

        } else if (empleado instanceof Cajero) {

            rol = "Cajero";

            if (empleado.getFechaIngreso() != null
                    && registro.getPeriodoInicio() != null) {

                aniosAntiguedad = aniosAntiguedad(
                        empleado.getFechaIngreso(),
                        registro.getPeriodoInicio());
            }

        } else {
            rol = "Empleado";
        }

        String nombreCompleto =
                (empleado.getNombre() != null ? empleado.getNombre() : "")
                + " "
                + (empleado.getApellido() != null ? empleado.getApellido() : "");

        String[] partes = nombreCompleto.trim().split(" ", 2);

        String emailEmpleado = empleado.getUsuario() != null
                ? empleado.getUsuario().getEmail()
                : "";

        // Construir el recibo con los datos guardados en la base de datos.
        Map<String, Object> reciboMap = new LinkedHashMap<>();

        reciboMap.put("liquidacionId", registro.getId());
        reciboMap.put("nombre", partes[0]);
        reciboMap.put("apellido", partes.length > 1 ? partes[1] : "");
        reciboMap.put("email", emailEmpleado);
        reciboMap.put("dni", empleado.getDni());
        reciboMap.put("rol", rol);
        reciboMap.put("festival", festival.getNombre());
        reciboMap.put("unidad", unidad.getNombreComercial());
        reciboMap.put("diasFestival", registro.getDiasTrabajados());
        reciboMap.put("sueldoBase", registro.getSueldoBase());
        reciboMap.put("plusCategoria", registro.getPlusCategoria());
        reciboMap.put("montoAntiguedad", registro.getMontoAntiguedad());
        reciboMap.put("aniosAntiguedad", aniosAntiguedad);
        reciboMap.put("coeficiente", registro.getFactorAplicado());
        reciboMap.put("total", registro.getMontoCalculado());
        reciboMap.put("periodoInicio", registro.getPeriodoInicio());
        reciboMap.put("periodoFin", registro.getPeriodoFin());
        reciboMap.put("fechaEmision", LocalDate.now());

        modelMap.put("recibo", reciboMap);
        modelMap.put("liquidacion", registro);
        modelMap.put("festival", festival);
        modelMap.put("unidad", unidad.getNombreComercial());
        modelMap.put("diasFestival", registro.getDiasTrabajados());

        return modelMap;
    }

    private UnidadVenta buscarUnidadDelResponsable(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al responsable autenticado.");
        }

        return unidadVentaRepository.findAll().stream()
                .filter(u -> u.getResponsable() != null
                        && u.getResponsable().getUsuario() != null
                        && u.getResponsable().getUsuario().getEmail() != null
                        && email.equalsIgnoreCase(
                                u.getResponsable().getUsuario().getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una unidad asignada al responsable indicado."));
    }

    private Festival buscarFestival(Long festivalId) {
        if (festivalId == null) {
            throw new IllegalArgumentException("Debe seleccionar un festival.");
        }

        return festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El festival seleccionado no existe."));
    }
}
