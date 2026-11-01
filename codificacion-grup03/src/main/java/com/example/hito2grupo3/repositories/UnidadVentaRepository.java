package com.example.hito2grupo3.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.entities.UnidadVenta;

public interface UnidadVentaRepository
        extends JpaRepository<UnidadVenta, Long> {

    @Query("SELECT u FROM UnidadVenta u WHERE u.id = :id")
    @EntityGraph(attributePaths = "platosOfrecidos")
    Optional<UnidadVenta> buscarPorIdConPlatos(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT u
    FROM UnidadVenta u
    JOIN u.staffAsignado s
    WHERE s.id = :staffId
""")
List<UnidadVenta> buscarPorStaffAsignado(@Param("staffId") Long staffId);
@Query("""
    SELECT DISTINCT u
    FROM UnidadVenta u
    LEFT JOIN u.staffAsignado s
    WHERE s.id = :staffId
       OR u.responsable.id = :staffId
""")
List<UnidadVenta> buscarPorStaffAsignadoOResponsable(
        @Param("staffId") Long staffId);

    // Verifica si ya existe una unidad con ese código
    boolean existsByCodigo(String codigo);
    Optional<UnidadVenta> findByCodigo(String codigo);
    Optional<UnidadVenta> findByCodigoIgnoreCase(String codigo);
    List<UnidadVenta> findByCodigoStartingWith(String prefijo);

    Optional<UnidadVenta> findByResponsable_Id(long responsableId);
}
