package com.example.hito2grupo3.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.entities.CategoriaCocina;
import com.example.hito2grupo3.repositories.CategoriaCocinaRepository;
import com.example.hito2grupo3.repositories.CocineroRepository;
import com.example.hito2grupo3.services.ICategoriaCocinaService;

@Service
public class CategoriaCocinaServiceImpl implements ICategoriaCocinaService {

    private final CategoriaCocinaRepository categoriaCocinaRepository;
    private final CocineroRepository cocineroRepository;

    public CategoriaCocinaServiceImpl(
            CategoriaCocinaRepository categoriaCocinaRepository,
            CocineroRepository cocineroRepository) {
        this.categoriaCocinaRepository = categoriaCocinaRepository;
        this.cocineroRepository = cocineroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocinaDTO> listarTodasDTO() {
        return categoriaCocinaRepository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocinaDTO> listarActivasDTO() {
        return categoriaCocinaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocina> listarTodas() {
        return categoriaCocinaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCocina> listarActivas() {
        return categoriaCocinaRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaCocinaDTO buscarDtoPorId(Long id) {
        CategoriaCocina entidad = buscarPorId(id);
        return convertirADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaCocina buscarPorId(Long id) {
        return categoriaCocinaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría de cocina con ID " + id));
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO crear(CategoriaCocinaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la categoría son obligatorios.");
        }

        String nombreLimpio = dto.getNombre() != null ? dto.getNombre().trim() : "";
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }

        // Buscar si ya existe una categoría con ese nombre
        Optional<CategoriaCocina> existenteOpt = categoriaCocinaRepository.findByNombreIgnoreCase(nombreLimpio);
        if (existenteOpt.isPresent()) {
            CategoriaCocina existente = existenteOpt.get();
            if (existente.isActivo()) {
                throw new IllegalArgumentException("Ya existe una categoría de cocina activa con el nombre '" + nombreLimpio + "'.");
            } else {
                // Si estaba dada de baja lógicamente, se reactiva y actualiza el plus fijo
                existente.setActivo(true);
                existente.setPlusFijo(dto.getPlusFijo());
                CategoriaCocina reactivada = categoriaCocinaRepository.save(existente);
                return convertirADTO(reactivada);
            }
        }

        CategoriaCocina entidad = new CategoriaCocina();
        entidad.setNombre(nombreLimpio);
        entidad.setPlusFijo(dto.getPlusFijo());
        entidad.setActivo(true);

        CategoriaCocina guardada = categoriaCocinaRepository.save(entidad);
        return convertirADTO(guardada);
    }

    @Override
    @Transactional
    public CategoriaCocinaDTO actualizar(Long id, CategoriaCocinaDTO dto) {
        CategoriaCocina entidad = buscarPorId(id);

        String nombreLimpio = dto.getNombre() != null ? dto.getNombre().trim() : "";
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }

        if (categoriaCocinaRepository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(nombreLimpio, id)) {
            throw new IllegalArgumentException("Ya existe otra categoría de cocina activa con el nombre '" + nombreLimpio + "'.");
        }

        entidad.setNombre(nombreLimpio);
        entidad.setPlusFijo(dto.getPlusFijo());

        CategoriaCocina actualizada = categoriaCocinaRepository.save(entidad);
        return convertirADTO(actualizada);
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        CategoriaCocina entidad = buscarPorId(id);
        entidad.setActivo(false);
        categoriaCocinaRepository.save(entidad);
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        CategoriaCocina entidad = buscarPorId(id);
        entidad.setActivo(true);
        categoriaCocinaRepository.save(entidad);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        darDeBaja(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarCocinerosAsociados(Long id) {
        return cocineroRepository.countByCategoriaCocina_Id(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.hito2grupo3.entities.Cocinero> listarCocinerosPorCategoria(Long categoriaId) {
        return cocineroRepository.findByCategoriaCocina_Id(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.hito2grupo3.entities.Cocinero> listarTodosLosCocineros() {
        return cocineroRepository.findAll();
    }

    @Override
    @Transactional
    public void asignarCocineroACategoria(Long categoriaId, Long cocineroId) {
        CategoriaCocina categoria = buscarPorId(categoriaId);
        com.example.hito2grupo3.entities.Cocinero cocinero = cocineroRepository.findById(cocineroId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cocinero con ID " + cocineroId));

        cocinero.setCategoriaCocina(categoria);
        cocineroRepository.save(cocinero);
    }

    @Override
    @Transactional
    public void desasignarCocineroDeCategoria(Long cocineroId) {
        com.example.hito2grupo3.entities.Cocinero cocinero = cocineroRepository.findById(cocineroId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cocinero con ID " + cocineroId));

        cocinero.setCategoriaCocina(null);
        cocineroRepository.save(cocinero);
    }

    private CategoriaCocinaDTO convertirADTO(CategoriaCocina entidad) {
        long cocineros = cocineroRepository.countByCategoriaCocina_Id(entidad.getId());
        CategoriaCocinaDTO dto = new CategoriaCocinaDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getPlusFijo(),
                entidad.isActivo()
        );
        dto.setCantidadCocinerosAsociados(cocineros);
        return dto;
    }
}
