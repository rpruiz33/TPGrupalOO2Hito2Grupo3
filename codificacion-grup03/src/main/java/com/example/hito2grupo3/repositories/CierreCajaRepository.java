package com.example.hito2grupo3.repositories;

import com.example.hito2grupo3.entities.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    List<CierreCaja> findByCajeroId(Long cajeroId);

    List<CierreCaja> findByUnidadVentaId(Long unidadVentaId);

    List<CierreCaja> findByFechaBetween(LocalDate desde, LocalDate hasta);

    List<CierreCaja> findByCajeroIdAndFechaBetween(Long cajeroId, LocalDate desde, LocalDate hasta);

    Optional<CierreCaja> findByCajeroIdAndFecha(Long cajeroId, LocalDate fecha);

    @Query("SELECT COALESCE(SUM(cc.montoRecaudado), 0) FROM CierreCaja cc WHERE cc.cajero.id = :cajeroId")
    BigDecimal sumarRecaudacionPorCajero(@Param("cajeroId") Long cajeroId);

    @Query("""
            SELECT COALESCE(SUM(cc.montoRecaudado), 0)
            FROM CierreCaja cc
            WHERE cc.cajero.id = :cajeroId
              AND cc.fecha >= :desde
              AND cc.fecha <= :hasta
            """)
    BigDecimal sumarRecaudacionPorCajeroYFechas(
            @Param("cajeroId") Long cajeroId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("""
            SELECT cc FROM CierreCaja cc
            WHERE cc.cajero.id = :cajeroId
            ORDER BY cc.fecha DESC
            """)
    List<CierreCaja> findByCajeroIdOrderByFechaDesc(@Param("cajeroId") Long cajeroId);
}
