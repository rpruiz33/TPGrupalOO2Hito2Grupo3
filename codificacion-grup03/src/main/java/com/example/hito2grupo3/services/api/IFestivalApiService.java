package com.example.hito2grupo3.services.api;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.FestivalDTO;

/**
 * Servicio REST para la gestión integral de festivales gastronómicos y sus temporadas.
 */
public interface IFestivalApiService {

    FestivalDTO crear(FestivalDTO request);

    FestivalDTO obtenerPorId(Long id);

    List<FestivalDTO> buscarFiltrados(
            String nombre,
            String temporada,
            Boolean activo,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Long unidadVentaId);

    FestivalDTO actualizar(Long id, FestivalDTO request);

    FestivalDTO darDeBaja(Long id);

    FestivalDTO reactivar(Long id);

    FestivalDTO habilitarUnidad(Long festivalId, Long unidadId);

    FestivalDTO desasociarUnidad(Long festivalId, Long unidadId);
}
