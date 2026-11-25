package com.example.hito2grupo3.services.api.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.dto.api.LiquidacionRequestDTO;
import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.Cocinero;
import com.example.hito2grupo3.entities.LiquidacionHaberes;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.LiquidacionHaberesRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.api.ILiquidacionApiService;

/**
 * Implementación del servicio REST para liquidación de haberes.
 * Reutiliza {@link LiquidacionDTO} y {@link ILiquidacionService.ResultadoLiquidacion}.
 */
@Service
public class LiquidacionApiServiceImpl implements ILiquidacionApiService {

    private final ILiquidacionService liquidacionService;
    private final LiquidacionHaberesRepository liquidacionRepository;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public LiquidacionApiServiceImpl(
            ILiquidacionService liquidacionService,
            LiquidacionHaberesRepository liquidacionRepository,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.liquidacionService = liquidacionService;
        this.liquidacionRepository = liquidacionRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    @Transactional
    public ILiquidacionService.ResultadoLiquidacion registrarLiquidaciones(LiquidacionRequestDTO request) {
        validarReferencias(request);
        return liquidacionService.registrarLiquidacionesSegunRol(
                normalizar(request.getEmailEmpleado()),
                request.getFestivalId(),
                request.getUnidadVentaId(),
                normalizar(request.getCodigoUnidad()),
                true,
                false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionDTO> buscarHistorialFiltrado(
            Long festivalId,
            Long unidadVentaId,
            String email,
            String rol,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal montoMinimo,
            BigDecimal montoMaximo) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (montoMinimo != null && montoMaximo != null && montoMinimo.compareTo(montoMaximo) > 0) {
            throw new IllegalArgumentException("El monto mínimo no puede superar al monto máximo.");
        }
        if (festivalId != null && !festivalRepository.existsById(festivalId)) {
            throw new NoSuchElementException("No existe el festival con id " + festivalId + ".");
        }
        if (unidadVentaId != null && !unidadVentaRepository.existsById(unidadVentaId)) {
            throw new NoSuchElementException("No existe la unidad de venta con id " + unidadVentaId + ".");
        }

        String emailNorm = normalizarLower(email);
        String rolNorm = normalizarLower(rol);

        return liquidacionRepository.findAll().stream()
                .filter(l -> festivalId == null
                        || (l.getFestival() != null && festivalId.equals(l.getFestival().getId())))
                .filter(l -> unidadVentaId == null
                        || (l.getUnidadVenta() != null && unidadVentaId.equals(l.getUnidadVenta().getId())))
                .filter(l -> fechaDesde == null
                        || (l.getPeriodoFin() != null && !l.getPeriodoFin().isBefore(fechaDesde)))
                .filter(l -> fechaHasta == null
                        || (l.getPeriodoInicio() != null && !l.getPeriodoInicio().isAfter(fechaHasta)))
                .filter(l -> montoMinimo == null
                        || (l.getMontoCalculado() != null && l.getMontoCalculado().compareTo(montoMinimo) >= 0))
                .filter(l -> montoMaximo == null
                        || (l.getMontoCalculado() != null && l.getMontoCalculado().compareTo(montoMaximo) <= 0))
                .map(this::mapearADto)
                .filter(dto -> emailNorm == null
                        || (dto.email() != null && dto.email().toLowerCase(Locale.ROOT).contains(emailNorm)))
                .filter(dto -> rolNorm == null
                        || (dto.rol() != null && dto.rol().toLowerCase(Locale.ROOT).equals(rolNorm)))
                .toList();
    }

    private LiquidacionDTO mapearADto(LiquidacionHaberes entidad) {
        Staff s = entidad.getStaff();
        String rol = resolverRol(s);
        String email = (s != null && s.getUsuario() != null) ? s.getUsuario().getEmail() : null;
        String nombre = s != null ? (s.getNombre() + " " + s.getApellido()) : null;
        String dni = s != null ? s.getDni() : null;
        Long empId = s != null ? s.getId() : null;
        Long uvId = entidad.getUnidadVenta() != null ? entidad.getUnidadVenta().getId() : null;
        String uvNombre = entidad.getUnidadVenta() != null ? entidad.getUnidadVenta().getNombreComercial() : null;

        return new LiquidacionDTO(
                empId,
                nombre,
                email,
                dni,
                rol,
                0L,
                entidad.getSueldoBase(),
                entidad.getMontoAntiguedad(),
                entidad.getPlusCategoria(),
                entidad.getMontoCalculado(),
                uvId,
                uvNombre,
                entidad.getPeriodoInicio(),
                entidad.getPeriodoFin(),
                entidad.getDiasTrabajados() != null ? entidad.getDiasTrabajados() : 0L,
                entidad.getFactorAplicado(),
                entidad.getId());
    }

    private String resolverRol(Staff s) {
        if (s instanceof Cocinero) {
            return "Cocinero";
        }
        if (s instanceof Cajero) {
            return "Cajero";
        }
        if (s != null && s.getUsuario() != null && s.getUsuario().getRol() != null) {
            String r = s.getUsuario().getRol().getNombre();
            if (r != null && r.toUpperCase(Locale.ROOT).contains("RESPONSABLE")) {
                return "Responsable";
            }
        }
        return "Empleado";
    }

    private void validarReferencias(LiquidacionRequestDTO request) {
        if (!festivalRepository.existsById(request.getFestivalId())) {
            throw new NoSuchElementException("No existe el festival con id " + request.getFestivalId() + ".");
        }
        if (request.getUnidadVentaId() != null && !unidadVentaRepository.existsById(request.getUnidadVentaId())) {
            throw new NoSuchElementException(
                    "No existe la unidad de venta con id " + request.getUnidadVentaId() + ".");
        }
    }

    private String normalizar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String normalizarLower(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase(Locale.ROOT);
    }
}
