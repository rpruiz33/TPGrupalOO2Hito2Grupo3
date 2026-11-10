package com.example.hito2grupo3.services;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de negocio para el panel de control del administrador.
 * Centraliza el cálculo de KPIs y métricas del dashboard.
 */
public interface IAdminDashboardService {

    /**
     * Obtiene todos los KPIs necesarios para el dashboard del administrador:
     * <ul>
     *   <li>festivalesCount – cantidad de festivales registrados</li>
     *   <li>usuariosCount – cantidad de usuarios en el sistema</li>
     *   <li>recaudacionMensual – recaudación del festival actual o vigente</li>
     *   <li>rentabilidadNetaTotal – rentabilidad neta del festival actual o vigente</li>
     *   <li>festivalActualNombre – nombre del festival actual o vigente</li>
     *   <li>festivalId – ID del festival seleccionado/vigente</li>
     *   <li>festivales – lista de todos los festivales disponibles</li>
     *   <li>unidadesCount – cantidad de unidades de venta activas</li>
     *   <li>staffCount – cantidad de personal / staff</li>
     *   <li>primerFestivalId – ID del festival actual o primer festival registrado (para enlaces)</li>
     * </ul>
     *
     * @return mapa con las claves descritas arriba
     */
    Map<String, Object> obtenerKpisDashboard();

    /**
     * Obtiene los KPIs del dashboard para un festival específico (o por defecto si es null).
     */
    Map<String, Object> obtenerKpisDashboard(Long festivalId);

    /**
     * Calcula la recaudación correspondiente al festival actual o por defecto.
     * Si no hay festival registrado retorna cero o la recaudación general.
     */
    BigDecimal calcularRecaudacionFestivalActual();

    /**
     * Calcula la recaudación del mes en curso.
     * Si no hay ventas en el mes retorna la recaudación total.
     */
    BigDecimal calcularRecaudacionMensual();
}
