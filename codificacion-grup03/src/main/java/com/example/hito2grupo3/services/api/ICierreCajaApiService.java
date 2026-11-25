package com.example.hito2grupo3.services.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.api.CierreCajaRequestDTO;
import com.example.hito2grupo3.dto.api.CierreCajaResponseDTO;
import com.example.hito2grupo3.dto.api.ReaperturaCajaRequestDTO;
import com.example.hito2grupo3.dto.api.ResumenCajaResponseDTO;

/**
 * Servicio REST para el cierre, reapertura y consulta filtrada de cajas.
 */
public interface ICierreCajaApiService {

    CierreCajaResponseDTO registrarCierre(CierreCajaRequestDTO request);

    CierreCajaResponseDTO reabrirCaja(ReaperturaCajaRequestDTO request);

    CierreCajaResponseDTO obtenerPorId(Long id);

    List<CierreCajaResponseDTO> buscarHistorialFiltrado(
            Long cajeroId,
            Long unidadVentaId,
            String estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal montoMinimo);

    ResumenCajaResponseDTO obtenerResumenPorCajero(String emailCajero, LocalDate fecha);
}

