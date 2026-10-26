package com.example.hito2grupo3.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.UnidadVenta;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    List<Festival> findAllByOrderByFechaInicioAsc();

    @Query("""
        SELECT u
        FROM Festival f
        JOIN f.unidadesVentaHabilitadas u
        WHERE f.id = :festivalId
        ORDER BY u.nombreComercial
        """)
    List<UnidadVenta> obtenerUnidadesPorFestival(
            @Param("festivalId") Long festivalId);


   
Optional<Festival> findFirstByOrderByIdAsc();
    

    @Query("""
    SELECT DISTINCT f
    FROM Festival f
    JOIN f.unidadesVentaHabilitadas u
    JOIN u.staffAsignado s
    WHERE s.id = :staffId
      AND f.fechaInicio <= :fecha
      AND f.fechaFin >= :fecha
    """)
List<Festival> buscarActivosPorStaff(
        @Param("staffId") Long staffId,
        @Param("fecha") LocalDate fecha);        
}
