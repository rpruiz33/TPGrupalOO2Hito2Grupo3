package com.example.hito2grupo3.services.api;

import java.math.BigDecimal;
import java.util.List;

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;

/**
 * Servicio REST para la gestión de categorías de cocina y asignación de cocineros.
 */
public interface ICategoriaCocinaApiService {

    CategoriaCocinaDTO crear(CategoriaCocinaDTO request);

    CategoriaCocinaDTO obtenerPorId(Long id);

    List<CategoriaCocinaDTO> buscarFiltradas(
            String nombre,
            Boolean activa,
            BigDecimal plusMinimo,
            BigDecimal plusMaximo);

    CategoriaCocinaDTO actualizar(Long id, CategoriaCocinaDTO request);

    CategoriaCocinaDTO darDeBaja(Long id);

    CategoriaCocinaDTO reactivar(Long id);

    CategoriaCocinaDTO asignarCocinero(Long categoriaId, Long cocineroId);
}
