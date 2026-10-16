package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionService implements ILiquidacionService {

    // Valores fijos requeridos definidos directamente en el servicio
    private static final BigDecimal SUELDO_BASE = new BigDecimal("100000");
    private static final BigDecimal VALOR_ANIO_ANTIGUEDAD = new BigDecimal("5000");

    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionService(FestivalRepository festivalRepository,
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
                && emailResponsable.equals(u.getResponsable().getUsuario().getEmail()))
            .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tenés una unidad de venta asignada."));

        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        
        // Fórmula proporcional solicitada: (int(diasDurFest / 15) + 1)
        int periodos = (int) (dias / 15) + 1;

        Set<LiquidacionDTO> conjuntoLiquidaciones = new HashSet<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Staff s : unidad.getStaffAsignado()) {
            long anios = aniosAntiguedad(s.getFechaIngreso(), festival.getFechaInicio());
            BigDecimal adicional;
            String rol;
            BigDecimal baseCalculo;

            if (s instanceof Cocinero c) {
                // Plus por categoría de cocina ($150.000, $100.000 o $50.000)
                adicional = c.getCategoriaCocina() != null ? c.getCategoriaCocina().getPlusFijo() : BigDecimal.ZERO;
                rol = c.getCategoriaCocina() != null ? c.getCategoriaCocina().getNombre() : "Cocinero";
                
                // (Sueldo Base + Plus por categoría)
                baseCalculo = SUELDO_BASE.add(adicional);
            } else if (s instanceof Cajero) {
                // Antigüedad: $5000 por cada año
                adicional = VALOR_ANIO_ANTIGUEDAD.multiply(BigDecimal.valueOf(anios));
                rol = "Cajero";
                
                // (Sueldo Base + Antigüedad)
                baseCalculo = SUELDO_BASE.add(adicional);
            } else {
                continue;
            }

            // totalAcobrar = (int(diasDurFest/15) + 1) * 50% * (baseCalculo)
            BigDecimal factorProporcional = BigDecimal.valueOf(periodos).multiply(BigDecimal.valueOf(0.5));
            BigDecimal total = baseCalculo.multiply(factorProporcional);

            totalGeneral = totalGeneral.add(total);
            
            conjuntoLiquidaciones.add(new LiquidacionDTO(
                    s.getId(), 
                    s.getNombre() + " " + s.getApellido(), 
                    s.getDni(),
                    rol, 
                    anios, 
                    SUELDO_BASE, 
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