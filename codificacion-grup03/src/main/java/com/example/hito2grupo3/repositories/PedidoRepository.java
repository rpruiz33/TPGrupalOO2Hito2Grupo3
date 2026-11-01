package com.example.hito2grupo3.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteVentaDTO;
import com.example.hito2grupo3.dto.VentaUnidadDiaDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // ============================
    // HISTORIAL DE PEDIDOS
    // ============================

    /**
     * Obtiene todos los pedidos de una unidad de venta,
     * tanto activos como anulados.
     * Incluye sus datos relacionados para mostrar el historial.
     */
    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            LEFT JOIN FETCH p.festival
            LEFT JOIN FETCH p.unidadVenta
            LEFT JOIN FETCH p.registradoPor
            LEFT JOIN FETCH p.anuladoPor
            LEFT JOIN FETCH p.items ip
            LEFT JOIN FETCH ip.plato
            WHERE p.unidadVenta.id = :unidadVentaId
            ORDER BY p.fechaRegistro DESC, p.id DESC
            """)
    List<Pedido> obtenerHistorialPorUnidad(
            @Param("unidadVentaId") Long unidadVentaId);


    // ============================
    // RECAUDACIÓN
    // ============================

    /**
     * Calcula la recaudación total.
     * Excluye pedidos anulados.
     */
    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM ItemPedido ip
            WHERE ip.pedido.estado = 'A'
            """)
    BigDecimal calcularRecaudacionTotal();


    /**
     * Calcula la recaudación entre dos fechas.
     * Excluye pedidos anulados.
     */
    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM ItemPedido ip
            WHERE ip.pedido.estado = 'A'
              AND ip.pedido.fechaTransaccion >= :desde
              AND ip.pedido.fechaTransaccion <= :hasta
            """)
    BigDecimal calcularRecaudacionEntreFechas(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);


    // ============================
    // VALIDACIONES
    // ============================

    boolean existsByFestival_Id(Long festivalId);


    // ============================
    // REPORTE POR FESTIVAL
    // ============================

    @Query("""
            SELECT new com.example.hito2grupo3.dto.ReporteVentaDTO(
                f.id,
                f.nombre,
                COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0),
                COUNT(DISTINCT p.id)
            )
            FROM Festival f
            LEFT JOIN Pedido p
                ON p.festival.id = f.id
                AND p.estado = 'A'
            LEFT JOIN ItemPedido ip
                ON ip.pedido.id = p.id
            WHERE f.id = :festivalId
            GROUP BY f.id, f.nombre
            """)
    ReporteVentaDTO obtenerReporteVentaPorFestival(
            @Param("festivalId") Long festivalId);


    // ============================
    // RANKING POR UNIDAD DE VENTA
    // ============================

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
            LEFT JOIN Pedido p
                ON p.unidadVenta.id = uv.id
                AND p.festival.id = :festivalId
                AND p.estado = 'A'
            LEFT JOIN ItemPedido ip
                ON ip.pedido.id = p.id
            WHERE f.id = :festivalId
            GROUP BY uv.id, uv.nombreComercial, uv.codigo, TYPE(uv)
            ORDER BY COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0) DESC
            """)
    List<RankingUnidadDTO> obtenerRankingPorFestival(
            @Param("festivalId") Long festivalId);


    // ============================
    // INGRESOS POR FESTIVAL Y FECHAS
    // ============================

    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM Pedido p
            JOIN p.items ip
            WHERE p.festival.id = :festivalId
              AND p.estado = 'A'
              AND (:fechaDesde IS NULL OR p.fechaTransaccion >= :fechaDesde)
              AND (:fechaHasta IS NULL OR p.fechaTransaccion <= :fechaHasta)
            """)
    BigDecimal sumarIngresosPorFestivalYFechas(
            @Param("festivalId") Long festivalId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta);


    // ============================
    // VENTAS POR UNIDAD Y FECHA
    // ============================

    /**
     * Calcula el total vendido por una unidad en una fecha.
     * Excluye pedidos anulados.
     */
    @Query("""
            SELECT COALESCE(SUM(ip.cantidad * ip.precioUnitario), 0)
            FROM Pedido p
            JOIN p.items ip
            WHERE p.unidadVenta.id = :unidadVentaId
              AND p.fechaTransaccion = :fecha
              AND p.estado = 'A'
            """)
    BigDecimal calcularTotalVentasPorUnidadYFecha(
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("fecha") LocalDate fecha);


    /**
     * Obtiene el detalle de ventas por plato de una unidad
     * en una fecha específica.
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
              AND p.estado = 'A'
            GROUP BY ip.plato.nombre, ip.precioUnitario
            ORDER BY ip.plato.nombre
            """)
    List<VentaUnidadDiaDTO> obtenerDetalleVentasPorUnidadYFecha(
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("fecha") LocalDate fecha);


    // ============================
    // PLATO MÁS VENDIDO
    // ============================

    /**
     * Obtiene el plato más vendido de una unidad
     * en un festival.
     * Excluye pedidos anulados.
     */
    @Query("""
            SELECT new com.example.hito2grupo3.dto.PlatoDTO(
                ip.plato.id,
                ip.plato.nombre,
                SUM(ip.cantidad),
                SUM(ip.cantidad * ip.precioUnitario)
            )
            FROM Pedido p
            JOIN p.items ip
            WHERE p.unidadVenta.id = :unidadVentaId
              AND p.festival.id = :festivalId
              AND p.estado = 'A'
            GROUP BY ip.plato.id, ip.plato.nombre
            ORDER BY SUM(ip.cantidad) DESC
            """)
    List<PlatoDTO> obtenerPlatosEstrella(
            @Param("unidadVentaId") Long unidadVentaId,
            @Param("festivalId") Long festivalId);


    // ============================
    // DETALLE DEL PEDIDO
    // ============================

    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            LEFT JOIN FETCH p.festival
            LEFT JOIN FETCH p.unidadVenta
            LEFT JOIN FETCH p.registradoPor
            LEFT JOIN FETCH p.anuladoPor
            LEFT JOIN FETCH p.items ip
            LEFT JOIN FETCH ip.plato
            WHERE p.id = :pedidoId
            """)
    Optional<Pedido> obtenerDetallePorId(
            @Param("pedidoId") Long pedidoId);

}