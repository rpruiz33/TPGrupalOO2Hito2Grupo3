package com.example.hito2grupo3.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hito2grupo3.entities.Plato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByUnidadVenta_IdOrderByNombreAsc(Long unidadVentaId);
}
