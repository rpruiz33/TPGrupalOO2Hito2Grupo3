package com.example.hito2grupo3.services.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.api.RentabilidadRequestDTO;

/**
 * Servicio REST para el cálculo y consulta filtrada de rentabilidad neta.
 */
public interface IRentabilidadApiService {

    ReporteRentabilidadNetaDTO calcularRentabilidadUnidad(RentabilidadRequestDTO request);

    List<ReporteRentabilidadNetaDTO> buscarRentabilidadFiltrada(
            Long festivalId,
            Long unidadVentaId,
            String tipoUnidad,
            String temporada,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal rentabilidadMinima);
}
