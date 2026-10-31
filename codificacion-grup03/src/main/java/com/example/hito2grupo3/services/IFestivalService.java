package com.example.hito2grupo3.services;

import java.util.List;

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.entities.Festival;

public interface IFestivalService {

    List<Festival> listarTodos();

    List<FestivalDTO> listarTodosDTO();

    Festival buscarPorId(Long id);

    FestivalDTO obtenerDtoPorId(Long id);

    Festival crearFestival(FestivalDTO dto);

    Festival actualizarFestival(Long id, FestivalDTO dto);

    void darDeBaja(Long id);

    void reactivar(Long id);

    void eliminarFestival(Long id);
}

