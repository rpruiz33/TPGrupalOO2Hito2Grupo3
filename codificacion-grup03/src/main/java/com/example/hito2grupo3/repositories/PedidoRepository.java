package com.example.hito2grupo3.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("""
        SELECT new com.example.hito2grupo3.dto.ReporteVentaDTO(
            f.id,
            f.nombre,
            COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0bd),
            COUNT(DISTINCT p.id)
        )
        FROM Festival f
        LEFT JOIN Pedido p ON p.festival.id = f.id
        LEFT JOIN ItemPedido ip ON ip.pedido.id = p.id
        WHERE f.id = :festivalId
        GROUP BY f.id, f.nombre
        """)
    ReporteVentaDTO obtenerReporteVentaPorFestival(@Param("festivalId") Long festivalId);

    @Query("""
        SELECT new com.example.hito2grupo3.dto.RankingUnidadDTO(
            uv.id,
            uv.nombreComercial,
            uv.codigo,
            CASE
                WHEN TYPE(uv) = FoodTruck THEN 'FoodTruck'
                WHEN TYPE(uv) = PuestoDesarmable THEN 'PuestoDesarmable'
                ELSE 'UnidadVenta'
            END,
            COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0bd)
        )
        FROM UnidadVenta uv
        JOIN uv.festivales f
        LEFT JOIN Pedido p ON p.unidadVenta.id = uv.id AND p.festival.id = f.id
        LEFT JOIN ItemPedido ip ON ip.pedido.id = p.id
        WHERE f.id = :festivalId
        GROUP BY uv.id, uv.nombreComercial, uv.codigo, TYPE(uv)
        ORDER BY COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0bd) DESC
        """)
    List<RankingUnidadDTO> obtenerRankingPorFestival(@Param("festivalId") Long festivalId);
}