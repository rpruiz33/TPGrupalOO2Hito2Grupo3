package com.example.hito2grupo3.repositories;

import java.util.Optional;

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

    Optional<UnidadVenta> findByResponsableUsuarioEmail(String email);
}
