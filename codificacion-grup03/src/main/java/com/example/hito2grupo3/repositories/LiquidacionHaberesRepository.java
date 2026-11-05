package com.example.hito2grupo3.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.entities.LiquidacionHaberes;

public interface LiquidacionHaberesRepository
        extends JpaRepository<LiquidacionHaberes, Long> {

    @Query("""
            SELECT l
            FROM LiquidacionHaberes l
            WHERE l.festival.id = :festivalId
              AND l.unidadVenta.id = :unidadVentaId
            ORDER BY l.periodoInicio ASC
            """)
    List<LiquidacionHaberes> buscarPorFestivalYUnidad(
            @Param("festivalId") Long festivalId,
            @Param("unidadVentaId") Long unidadVentaId);

                @Query("""
            SELECT l
            FROM LiquidacionHaberes l
            WHERE l.festival.id = :festivalId
            ORDER BY l.unidadVenta.id ASC, l.periodoInicio ASC
            """)
    List<LiquidacionHaberes> buscarPorFestival(
            @Param("festivalId") Long festivalId);
    @Query("""
            SELECT l
            FROM LiquidacionHaberes l
            WHERE l.staff.id = :staffId
              AND l.festival.id = :festivalId
            ORDER BY l.periodoInicio ASC
            """)
    List<LiquidacionHaberes> buscarPorEmpleadoYFestival(
            @Param("staffId") Long staffId,
            @Param("festivalId") Long festivalId);

    @Query("""
            SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END
            FROM LiquidacionHaberes l
            WHERE l.staff.id = :staffId
              AND l.unidadVenta.id = :unidadVentaId
              AND l.festival.id = :festivalId
              AND l.periodoInicio = :periodoInicio
              AND l.periodoFin = :periodoFin
            """)
    boolean existeLiquidacionParaPeriodo(
            @Param("staffId") Long staffId,
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("festivalId") Long festivalId,
            @Param("periodoInicio") LocalDate periodoInicio,
            @Param("periodoFin") LocalDate periodoFin);
}