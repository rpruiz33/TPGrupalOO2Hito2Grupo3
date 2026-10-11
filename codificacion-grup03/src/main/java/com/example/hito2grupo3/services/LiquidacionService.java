package com.example.hito2grupo3.services;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.Cocinero;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiquidacionService {

    public record ResultadoLiquidacion(String unidad,
                                       long diasFestival,
                                       List<LiquidacionDTO> liquidaciones,
                                       BigDecimal totalGeneral) {
    }

    private final SueldoProperties sueldos;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionService(SueldoProperties sueldos,
                              FestivalRepository festivalRepository,
                              UnidadVentaRepository unidadVentaRepository) {
        this.sueldos = sueldos;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Transactional(readOnly = true)
    public ResultadoLiquidacion liquidar(String emailResponsable, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("El festival seleccionado no existe."));

        UnidadVenta unidad = unidadVentaRepository.findByResponsableUsuarioEmail(emailResponsable)
                .orElseThrow(() -> new IllegalArgumentException("No tenés una unidad de venta asignada."));

        long dias = diasDelFestival(festival.getFechaInicio(), festival.getFechaFin());
        int periodos = periodos(dias);

        List<LiquidacionDTO> lista = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Staff s : unidad.getStaffAsignado()) {
            long anios = aniosAntiguedad(s.getFechaIngreso(), festival.getFechaInicio());
            BigDecimal adicional;
            String rol;

            if (s instanceof Cocinero c) {
                adicional = c.getCategoriaCocina().getPlusFijo();
                rol = c.getCategoriaCocina().getNombre();
            } else if (s instanceof Cajero) {
                adicional = antiguedadTotal(anios);
                rol = "Cajero";
            } else {
                continue;
            }

            BigDecimal total = total(dias, adicional);
            totalGeneral = totalGeneral.add(total);
            lista.add(new LiquidacionDTO(s.getId(), s.getNombre() + " " + s.getApellido(), s.getDni(),
                    rol, anios, sueldos.base(), adicional, periodos, total));
        }
        return new ResultadoLiquidacion(unidad.getNombreComercial(), dias, lista, totalGeneral);
    }

    /** Duración del festival en días, contando inicio y fin. */
    public long diasDelFestival(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El período del festival no es válido.");
        }
        return ChronoUnit.DAYS.between(inicio, fin) + 1;
    }

    /** Cada tramo (o fracción) de 15 días paga el 50%: 1-15 -> 50%, 16-30 -> 100%, 31-45 -> 150%... */
    public int periodos(long diasFestival) {
        return (int) Math.ceil((double) diasFestival / sueldos.diasPorPeriodo());
    }

    public long aniosAntiguedad(LocalDate fechaIngreso, LocalDate referencia) {
        if (fechaIngreso == null || fechaIngreso.isAfter(referencia)) {
            return 0;
        }
        return ChronoUnit.YEARS.between(fechaIngreso, referencia);
    }

    public BigDecimal antiguedadTotal(long anios) {
        return sueldos.porAnioAntiguedad().multiply(BigDecimal.valueOf(anios));
    }

    /** total = periodos * 50% * (sueldoBase + adicional) */
    public BigDecimal total(long diasFestival, BigDecimal adicional) {
        return sueldos.base().add(adicional)
                .multiply(sueldos.porcentajePorPeriodo())
                .multiply(BigDecimal.valueOf(periodos(diasFestival)));
    }
}
