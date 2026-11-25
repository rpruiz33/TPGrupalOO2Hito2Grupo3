package com.example.hito2grupo3.services.api.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IFestivalService;
import com.example.hito2grupo3.services.api.IFestivalApiService;

/**
 * Implementación del servicio REST para festivales gastronómicos.
 * Reutiliza {@link IFestivalService} y {@link FestivalDTO} sin duplicar DTOs.
 */
@Service
public class FestivalApiServiceImpl implements IFestivalApiService {

    private final IFestivalService festivalService;
    private final FestivalRepository festivalRepository;
    private final UnidadVentaRepository unidadVentaRepository;

    public FestivalApiServiceImpl(
            IFestivalService festivalService,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository) {
        this.festivalService = festivalService;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    @Transactional
    public FestivalDTO crear(FestivalDTO request) {
        validarFechasRequest(request.getFechaInicio(), request.getFechaFin());
        if (request.getTemporada() == null || request.getTemporada().isBlank()) {
            request.setTemporada(Festival.determinarTemporada(request.getFechaInicio()));
        }
        Festival creado = festivalService.crearFestival(request);
        return festivalService.obtenerDtoPorId(creado.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FestivalDTO obtenerPorId(Long id) {
        buscarFestivalOExcepcion(id);
        return festivalService.obtenerDtoPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FestivalDTO> buscarFiltrados(
            String nombre,
            String temporada,
            Boolean activo,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Long unidadVentaId) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        String nombreNorm = normalizar(nombre);
        String temporadaNorm = normalizar(temporada);
        String estadoNorm = normalizar(estado);

        return festivalService.listarTodosDTO().stream()
                .filter(f -> nombreNorm == null
                        || (f.getNombre() != null && f.getNombre().toLowerCase(Locale.ROOT).contains(nombreNorm)))
                .filter(f -> temporadaNorm == null
                        || (f.getTemporada() != null && f.getTemporada().toLowerCase(Locale.ROOT).equals(temporadaNorm)))
                .filter(f -> activo == null || f.isActivo() == activo)
                .filter(f -> fechaDesde == null
                        || (f.getFechaFin() != null && !f.getFechaFin().isBefore(fechaDesde)))
                .filter(f -> fechaHasta == null
                        || (f.getFechaInicio() != null && !f.getFechaInicio().isAfter(fechaHasta)))
                .filter(f -> unidadVentaId == null
                        || (f.getUnidadesVentaIds() != null && f.getUnidadesVentaIds().contains(unidadVentaId)))
                .filter(f -> estadoNorm == null
                        || (f.getEstado() != null && f.getEstado().toLowerCase(Locale.ROOT).equals(estadoNorm)))
                .toList();
    }

    @Override
    @Transactional
    public FestivalDTO actualizar(Long id, FestivalDTO request) {
        buscarFestivalOExcepcion(id);
        validarFechasRequest(request.getFechaInicio(), request.getFechaFin());
        if (request.getTemporada() == null || request.getTemporada().isBlank()) {
            request.setTemporada(Festival.determinarTemporada(request.getFechaInicio()));
        }
        festivalService.actualizarFestival(id, request);
        return festivalService.obtenerDtoPorId(id);
    }

    @Override
    @Transactional
    public FestivalDTO darDeBaja(Long id) {
        buscarFestivalOExcepcion(id);
        festivalService.darDeBaja(id);
        return festivalService.obtenerDtoPorId(id);
    }

    @Override
    @Transactional
    public FestivalDTO reactivar(Long id) {
        buscarFestivalOExcepcion(id);
        festivalService.reactivar(id);
        return festivalService.obtenerDtoPorId(id);
    }

    @Override
    @Transactional
    public FestivalDTO habilitarUnidad(Long festivalId, Long unidadId) {
        Festival festival = buscarFestivalOExcepcion(festivalId);
        UnidadVenta unidad = unidadVentaRepository.findById(unidadId)
                .orElseThrow(() -> new NoSuchElementException("No existe la unidad de venta con id " + unidadId + "."));

        if (!unidad.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede habilitar una unidad de venta dada de baja lógica.");
        }

        festival.getUnidadesVentaHabilitadas().add(unidad);
        festivalRepository.save(festival);
        return festivalService.obtenerDtoPorId(festivalId);
    }

    @Override
    @Transactional
    public FestivalDTO desasociarUnidad(Long festivalId, Long unidadId) {
        buscarFestivalOExcepcion(festivalId);
        if (!unidadVentaRepository.existsById(unidadId)) {
            throw new NoSuchElementException("No existe la unidad de venta con id " + unidadId + ".");
        }
        festivalService.desasociarUnidadDeFestival(festivalId, unidadId);
        return festivalService.obtenerDtoPorId(festivalId);
    }

    private Festival buscarFestivalOExcepcion(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el festival con id " + id + "."));
    }

    private void validarFechasRequest(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La fecha de finalización debe ser al menos 1 día posterior a la fecha de inicio.");
        }
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim().toLowerCase(Locale.ROOT);
    }
}
