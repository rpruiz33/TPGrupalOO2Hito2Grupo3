package com.example.hito2grupo3.services.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.dto.api.LiquidacionRequestDTO;
import com.example.hito2grupo3.services.ILiquidacionService;

/**
 * Servicio REST para cálculo y consulta filtrada de liquidaciones de haberes.
 */
public interface ILiquidacionApiService {

    ILiquidacionService.ResultadoLiquidacion registrarLiquidaciones(LiquidacionRequestDTO request);

    List<LiquidacionDTO> buscarHistorialFiltrado(
            Long festivalId,
            Long unidadVentaId,
            String email,
            String rol,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal montoMinimo,
            BigDecimal montoMaximo);
}
