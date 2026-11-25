package com.example.hito2grupo3.services.api.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;
import com.example.hito2grupo3.repositories.CocineroRepository;
import com.example.hito2grupo3.services.ICategoriaCocinaService;
import com.example.hito2grupo3.services.api.ICategoriaCocinaApiService;

/**
 * Implementación del servicio REST para categorías de cocina.
 * Reutiliza {@link CategoriaCocinaDTO} sin DTOs redundantes.
 */
@Service
public class CategoriaCocinaApiServiceImpl implements ICategoriaCocinaApiService {

    private final ICategoriaCocinaService categoriaCocinaService;
    private final CategoriaCocinaRepository categoriaCocinaRepository;
    private final CocineroRepository cocineroRepository;

    public CategoriaCocinaApiServiceImpl(
            ICategoriaCocinaService categoriaCocinaService,
            CategoriaCocinaRepository categoriaCocinaRepository,
            CocineroRepository cocineroRepository) {
        this.categoriaCocinaService = categoriaCocinaService;
        this.categoriaCocinaRepository = categoriaCocinaRepository;
        this.cocineroRepository = cocineroRepository;
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO crear(CategoriaCocinaDTO request) {
        request.setActivo(true);
        return categoriaCocinaService.crear(request);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaCocinaDTO obtenerPorId(Long id) {
        verificarExistencia(id);
        return categoriaCocinaService.buscarDtoPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocinaDTO> buscarFiltradas(
            String nombre,
            Boolean activa,
            BigDecimal plusMinimo,
            BigDecimal plusMaximo) {

        if (plusMinimo != null && plusMaximo != null && plusMinimo.compareTo(plusMaximo) > 0) {
            throw new IllegalArgumentException("El plus mínimo no puede superar al plus máximo.");
        }

        String nombreNorm = (nombre == null || nombre.isBlank()) ? null : nombre.trim().toLowerCase(Locale.ROOT);

        return categoriaCocinaService.listarTodasDTO().stream()
                .filter(c -> nombreNorm == null
                        || (c.getNombre() != null && c.getNombre().toLowerCase(Locale.ROOT).contains(nombreNorm)))
                .filter(c -> activa == null || c.isActivo() == activa)
                .filter(c -> plusMinimo == null
                        || (c.getPlusFijo() != null && c.getPlusFijo().compareTo(plusMinimo) >= 0))
                .filter(c -> plusMaximo == null
                        || (c.getPlusFijo() != null && c.getPlusFijo().compareTo(plusMaximo) <= 0))
                .toList();
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO actualizar(Long id, CategoriaCocinaDTO request) {
        verificarExistencia(id);
        request.setId(id);
        return categoriaCocinaService.actualizar(id, request);
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO darDeBaja(Long id) {
        verificarExistencia(id);
        categoriaCocinaService.darDeBaja(id);
        return categoriaCocinaService.buscarDtoPorId(id);
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO reactivar(Long id) {
        verificarExistencia(id);
        categoriaCocinaService.reactivar(id);
        return categoriaCocinaService.buscarDtoPorId(id);
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO asignarCocinero(Long categoriaId, Long cocineroId) {
        verificarExistencia(categoriaId);
        if (!cocineroRepository.existsById(cocineroId)) {
            throw new NoSuchElementException("No existe el cocinero con id " + cocineroId + ".");
        }
        categoriaCocinaService.asignarCocineroACategoria(categoriaId, cocineroId);
        return categoriaCocinaService.buscarDtoPorId(categoriaId);
    }

    private void verificarExistencia(Long id) {
        if (!categoriaCocinaRepository.existsById(id)) {
            throw new NoSuchElementException("No existe la categoría de cocina con id " + id + ".");
        }
    }
}
