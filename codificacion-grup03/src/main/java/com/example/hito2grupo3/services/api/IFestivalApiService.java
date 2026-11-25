package com.example.hito2grupo3.services.api;

import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.FestivalDTO;

/**
 * Servicio REST para la gestión de festivales gastronómicos y sus temporadas.
 */
public interface IFestivalApiService {

    FestivalDTO crear(FestivalDTO request);

    List<FestivalDTO> buscarFiltrados(
            String nombre,
            String temporada,
            Boolean activo,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Long unidadVentaId);
}
