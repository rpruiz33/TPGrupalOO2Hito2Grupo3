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
}
