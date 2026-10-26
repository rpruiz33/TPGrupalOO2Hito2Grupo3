package com.example.hito2grupo3.repositories;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.hito2grupo3.entities.Costos;

@Repository
public interface CostoRepository extends JpaRepository<Costos, Long> {

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Costos c")
    BigDecimal obtenerTotalCostos();
}