package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionServiceImpl implements ILiquidacionService {

    private static final BigDecimal SUELDO_BASE = new BigDecimal("100000");
    private static final BigDecimal VALOR_ANIO_ANTIGUEDAD = new BigDecimal("5000");

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionServiceImpl(FestivalRepository festivalRepository,
                              UnidadVentaRepository unidadVentaRepository) {
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
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

    private ResultadoLiquidacion calcularLiquidacion(UnidadVenta unidad, Festival festival) {
        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        int periodos = (int) (dias / 15) + 1;

        Set<LiquidacionDTO> conjuntoLiquidaciones = new LinkedHashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        Set<Staff> personal = new LinkedHashSet<>();
        if (unidad.getResponsable() != null) {
            personal.add(unidad.getResponsable());
        }
        if (unidad.getStaffAsignado() != null) {
            personal.addAll(unidad.getStaffAsignado());
        }

        for (Staff s : personal) {
            long anios = aniosAntiguedad(s.getFechaIngreso(), festival.getFechaInicio());
            BigDecimal sueldoEmp = s.getSueldo() != null ? s.getSueldo() : SUELDO_BASE;
            BigDecimal adicional = BigDecimal.ZERO;
            String rol;

            if (s instanceof Cocinero c) {
                BigDecimal plusCat = (c.getCategoriaCocina() != null && c.getCategoriaCocina().getPlusFijo() != null)
                        ? c.getCategoriaCocina().getPlusFijo() : BigDecimal.ZERO;
                BigDecimal bonoPelig = c.getBonoPeligrosidad() != null ? c.getBonoPeligrosidad() : BigDecimal.ZERO;
                adicional = plusCat.add(bonoPelig);
                rol = c.getCategoriaCocina() != null ? c.getCategoriaCocina().getNombre() : "Cocinero";
            } else if (s instanceof Cajero caj) {
                BigDecimal plusAntig = VALOR_ANIO_ANTIGUEDAD.multiply(BigDecimal.valueOf(anios));
                BigDecimal plusOad = caj.getPlusOadicional() != null ? caj.getPlusOadicional() : BigDecimal.ZERO;
                adicional = plusAntig.add(plusOad);
                rol = "Cajero";
            } else {
                continue;
            }

            BigDecimal baseCalculo = sueldoEmp.add(adicional);
            BigDecimal factorProporcional = BigDecimal.valueOf(periodos).multiply(BigDecimal.valueOf(0.5));
            BigDecimal total = baseCalculo.multiply(factorProporcional);

            totalGeneral = totalGeneral.add(total);

            conjuntoLiquidaciones.add(new LiquidacionDTO(
                    s.getId(),
                    s.getNombre() + " " + s.getApellido(),
                    s.getDni(),
                    rol,
                    anios,
                    sueldoEmp,
                    adicional,
                    periodos,
                    total
            ));
        }

        return new ResultadoLiquidacion(unidad.getNombreComercial(), dias, conjuntoLiquidaciones, totalGeneral);
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