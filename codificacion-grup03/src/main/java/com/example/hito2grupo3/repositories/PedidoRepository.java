
package com.example.hito2grupo3.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.dto.VentaUnidadDiaDTO;
import com.example.hito2grupo3.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0) FROM ItemPedido ip")
    BigDecimal calcularRecaudacionTotal();

    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM ItemPedido ip
            WHERE ip.pedido.fechaTransaccion >= :desde
              AND ip.pedido.fechaTransaccion <= :hasta
            """)
    BigDecimal calcularRecaudacionEntreFechas(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);


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
    ReporteVentaDTO obtenerReporteVentaPorFestival(
            @Param("festivalId") Long festivalId);

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
            JOIN uv.festivales f
            LEFT JOIN Pedido p ON p.unidadVenta.id = uv.id
                AND p.festival.id = :festivalId
            LEFT JOIN ItemPedido ip ON ip.pedido.id = p.id
            WHERE f.id = :festivalId
            GROUP BY uv.id, uv.nombreComercial, uv.codigo, TYPE(uv)
            ORDER BY COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0) DESC
            """)
    List<RankingUnidadDTO> obtenerRankingPorFestival(
            @Param("festivalId") Long festivalId);

    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0) 
            FROM Pedido p 
            JOIN p.items ip 
            WHERE p.festival.id = :festivalId 
              AND (:fechaDesde IS NULL OR p.fechaTransaccion >= :fechaDesde) 
              AND (:fechaHasta IS NULL OR p.fechaTransaccion <= :fechaHasta)
            """)
    BigDecimal sumarIngresosPorFestivalYFechas(
            @Param("festivalId") Long festivalId, 
            @Param("fechaDesde") LocalDate fechaDesde, 
            @Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Calcula el total recaudado por una unidad de venta en una fecha específica.
     */
    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM Pedido p
            JOIN p.items ip
            WHERE p.unidadVenta.id = :unidadVentaId
              AND p.fechaTransaccion = :fecha
            """)
    BigDecimal calcularTotalVentasPorUnidadYFecha(
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("fecha") LocalDate fecha);

    /**
     * Obtiene el detalle de ventas por plato de una unidad de venta en una fecha específica.
     */
    @Query("""
            SELECT new com.example.hito2grupo3.dto.VentaUnidadDiaDTO(
                ip.plato.nombre,
                SUM(ip.cantidad),
                ip.precioUnitario,
                SUM(ip.cantidad * ip.precioUnitario)
            )
            FROM Pedido p
            JOIN p.items ip
            WHERE p.unidadVenta.id = :unidadVentaId
              AND p.fechaTransaccion = :fecha
            GROUP BY ip.plato.nombre, ip.precioUnitario
            ORDER BY ip.plato.nombre
            """)
    List<VentaUnidadDiaDTO> obtenerDetalleVentasPorUnidadYFecha(
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("fecha") LocalDate fecha);
}
