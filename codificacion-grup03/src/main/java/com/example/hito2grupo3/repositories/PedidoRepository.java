package com.example.hito2grupo3.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Req. 2: Agregación en DB para evitar carga masiva en la JVM.
    @Query("""
            SELECT new com.example.hito2grupo3.dto.ReporteVentaDTO(
                f.id,
                f.nombre,
                COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0),
                COUNT(DISTINCT p.id)
            )
            FROM Festival f
            LEFT JOIN Pedido p ON p.festival.id = f.id
            LEFT JOIN ItemPedido ip ON ip.pedido.id = p.id
            WHERE f.id = :festivalId
            GROUP BY f.id, f.nombre
            """)
    ReporteVentaDTO obtenerReporteVentaPorFestival(@Param("festivalId") Long festivalId);

    // Req. 2: Ranking por unidad con LEFT JOIN + GROUP BY + ORDER BY descendente.
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
                COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            )
            FROM UnidadVenta uv
            LEFT JOIN Pedido p ON p.unidadVenta.id = uv.id AND p.festival.id = :festivalId
            LEFT JOIN ItemPedido ip ON ip.pedido.id = p.id
            WHERE uv.festival.id = :festivalId
            GROUP BY uv.id, uv.nombreComercial, uv.codigo, TYPE(uv)
            ORDER BY COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0) DESC
            """)
    List<RankingUnidadDTO> obtenerRankingPorFestival(@Param("festivalId") Long festivalId);
}
