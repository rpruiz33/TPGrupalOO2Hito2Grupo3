package com.example.hito2grupo3.services;

import java.util.List;

import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.entities.CategoriaCocina;
import com.example.hito2grupo3.entities.Cocinero;

public interface ICategoriaCocinaService {

    List<CategoriaCocinaDTO> listarTodasDTO();

    List<CategoriaCocinaDTO> listarActivasDTO();

    List<CategoriaCocina> listarTodas();

    List<CategoriaCocina> listarActivas();

    CategoriaCocinaDTO buscarDtoPorId(Long id);

    CategoriaCocina buscarPorId(Long id);

    CategoriaCocinaDTO crear(CategoriaCocinaDTO dto);

    CategoriaCocinaDTO actualizar(Long id, CategoriaCocinaDTO dto);

    void darDeBaja(Long id);

    void reactivar(Long id);

    void eliminar(Long id);

    long contarCocinerosAsociados(Long id);

    List<Cocinero> listarCocinerosPorCategoria(Long categoriaId);

    List<Cocinero> listarTodosLosCocineros();

    void asignarCocineroACategoria(Long categoriaId, Long cocineroId);

    void desasignarCocineroDeCategoria(Long cocineroId);
}
