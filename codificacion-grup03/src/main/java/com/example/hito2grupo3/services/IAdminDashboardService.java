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
     *   <li>recaudacionMensual – recaudación del mes actual (o total si no hay del mes)</li>
     *   <li>unidadesCount – cantidad de unidades de venta</li>
     *   <li>staffCount – cantidad de personal / staff</li>
     *   <li>primerFestivalId – ID del primer festival registrado (para enlaces)</li>
     * </ul>
     *
     * @return mapa con las claves descritas arriba
     */
    Map<String, Object> obtenerKpisDashboard();

    /**
     * Calcula la recaudación del mes en curso.
     * Si no hay ventas en el mes retorna la recaudación total.
     */
    BigDecimal calcularRecaudacionMensual();
}
