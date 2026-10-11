package com.example.hito2grupo3.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query("""
        SELECT u
        FROM Festival f
        JOIN f.unidadesVentaHabilitadas u
        WHERE f.id = :festivalId
        ORDER BY u.nombreComercial
        """)
    List<UnidadVenta> obtenerUnidadesPorFestival(
            @Param("festivalId") Long festivalId);
}
