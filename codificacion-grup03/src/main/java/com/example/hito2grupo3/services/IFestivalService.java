package com.example.hito2grupo3.services;

import java.util.List;

import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.entities.Festival;
<<<<<<< HEAD
import com.example.hito2grupo3.entities.UnidadVenta;
=======
>>>>>>> 743b9b9 (ABM FESTIVAL BAJA LOGICA)

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
<<<<<<< HEAD

    /**
     * Retorna todas las unidades de venta disponibles en el sistema
     * para ser seleccionadas al crear o editar un festival.
     */
    List<UnidadVenta> listarTodasUnidades();

    /**
     * Busca los festivales activos para una unidad de venta en una fecha determinada.
     */
    List<Festival> buscarFestivalActivoPorUnidadVenta(Long unidadVentaId, java.time.LocalDate fecha);
}


=======
}

>>>>>>> 743b9b9 (ABM FESTIVAL BAJA LOGICA)
