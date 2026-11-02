package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.CierreCaja;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ICierreCajaService {

    /**
     * Registra un cierre de caja para un cajero en una unidad de venta.
     *
     * @param cajeroId       ID del cajero que realiza el cierre.
     * @param unidadVentaId  ID de la unidad de venta.
     * @param fecha          Fecha del cierre (normalmente hoy).
     * @param montoRecaudado Monto recaudado en el turno.
     * @return El CierreCaja persistido.
     */
    CierreCaja registrarCierre(Long cajeroId, Long unidadVentaId, LocalDate fecha, BigDecimal montoRecaudado);

    /**
     * Obtiene el historial de cierres de un cajero específico.
     */
    List<CierreCaja> obtenerHistorialPorCajero(Long cajeroId);

    /**
     * Obtiene el historial de cierres de un cajero en un rango de fechas.
     */
    List<CierreCaja> obtenerHistorialPorCajeroYFechas(Long cajeroId, LocalDate desde, LocalDate hasta);

    /**
     * Calcula la recaudación total acumulada de un cajero.
     */
    BigDecimal calcularRecaudacionTotal(Long cajeroId);

    /**
     * Calcula la recaudación de un cajero en un rango de fechas.
     */
    BigDecimal calcularRecaudacionEntreFechas(Long cajeroId, LocalDate desde, LocalDate hasta);

    /**
     * Obtiene el resumen del cierre para la vista, buscando el cajero por email.
     */
    Map<String, Object> obtenerResumenCierre(String emailCajero);

    /**
     * Reabre la caja para la fecha y unidad indicadas permitiendo continuar operando.
     *
     * @param cajeroId      ID del cajero.
     * @param unidadVentaId ID de la unidad de venta.
     * @param fecha         Fecha del cierre a reabrir.
     * @param motivo        Motivo opcional de la reapertura.
     * @return El CierreCaja en estado REABIERTA.
     */
    CierreCaja reabrirCaja(Long cajeroId, Long unidadVentaId, LocalDate fecha, String motivo);

    /**
     * Obtiene el cierre de caja de un cajero para una unidad de venta y fecha específicas.
     */
    java.util.Optional<CierreCaja> obtenerCierre(Long cajeroId, Long unidadVentaId, LocalDate fecha);

    /**
     * Valida y registra un cierre de caja. Aplica todas las reglas de negocio:
     * fecha no futura, monto no negativo con máximo 2 decimales, pertenencia
     * del cajero a la unidad de venta.
     *
     * @param cajeroId      ID del cajero
     * @param unidadVentaId ID de la unidad de venta
     * @param fecha         fecha del cierre (nula = hoy)
     * @param monto         monto recaudado (nulo = 0)
     * @param esAdmin       true si el usuario tiene rol ADMIN
     * @throws IllegalArgumentException si alguna validación de negocio falla
     */
    CierreCaja validarYRegistrarCierre(
            Long cajeroId,
            Long unidadVentaId,
            LocalDate fecha,
            java.math.BigDecimal monto,
            boolean esAdmin);

    /**
     * Retorna las unidades de venta que el cajero tiene asignadas.
     *
     * @param cajeroId ID del cajero
     * @param esAdmin  si es admin devuelve todas las unidades
     * @return lista de unidades accesibles para el cajero
     */
    java.util.List<com.example.hito2grupo3.entities.UnidadVenta> resolverUnidadesDelCajero(
            Long cajeroId, boolean esAdmin);
}
