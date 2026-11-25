package com.example.hito2grupo3.controllers.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.dto.ApiErrorDTO;
import com.example.hito2grupo3.dto.RankingUnidadDTO;
import com.example.hito2grupo3.dto.ReporteRentabilidadNetaDTO;
import com.example.hito2grupo3.dto.api.DashboardResumenResponseDTO;
import com.example.hito2grupo3.dto.api.RentabilidadRequestDTO;
import com.example.hito2grupo3.services.api.IRentabilidadApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Hito 3 - API REST de Rentabilidad Neta, Recaudación y KPIs (rpruiz33).
 */
@RestController
@RequestMapping(value = "/api/v1/rentabilidad", produces = "application/json")
@Tag(name = "Rentabilidad y Recaudación", description = "Cálculo de rentabilidad neta, recaudación por unidad y métricas de dashboard")
public class RentabilidadRestController {

    private final IRentabilidadApiService rentabilidadApiService;

    public RentabilidadRestController(IRentabilidadApiService rentabilidadApiService) {
        this.rentabilidadApiService = rentabilidadApiService;
    }

    @PostMapping("/calcular")
    @Operation(
            summary = "Calcular rentabilidad neta de una unidad en un festival",
            description = "Calcula ingresos totales, costo de platos, sueldos proporcionales y canon proporcional "
                    + "para determinar la rentabilidad neta sin fallbacks silenciosos a cero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo realizado exitosamente", content = @Content(schema = @Schema(implementation = ReporteRentabilidadNetaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Festival o unidad no encontrados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<ReporteRentabilidadNetaDTO> calcularRentabilidad(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Parámetros de cálculo de rentabilidad",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "festivalId": 1,
                              "unidadVentaId": 1
                            }
                            """)))
            @Valid @RequestBody RentabilidadRequestDTO request) {
        return ResponseEntity.ok(rentabilidadApiService.calcularRentabilidadUnidad(request));
    }

    @GetMapping
    @Operation(
            summary = "Consultar y filtrar rentabilidad neta",
            description = "Consulta el análisis de rentabilidad neta filtrando por hasta 7 parámetros: "
                    + "festival, unidad de venta, tipo de unidad, temporada, rango de fechas y rentabilidad mínima.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de análisis de rentabilidad"),
            @ApiResponse(responseCode = "400", description = "Filtros inválidos", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Festival o unidad no encontrados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<List<ReporteRentabilidadNetaDTO>> consultarRentabilidad(
            @Parameter(description = "ID del festival", example = "1")
            @RequestParam(required = false) Long festivalId,

            @Parameter(description = "ID de la unidad de venta", example = "1")
            @RequestParam(required = false) Long unidadVentaId,

            @Parameter(description = "Tipo de unidad de venta", schema = @Schema(allowableValues = { "FoodTruck", "PuestoDesarmable" }))
            @RequestParam(required = false) String tipoUnidad,

            @Parameter(description = "Temporada del festival", schema = @Schema(allowableValues = { "Invierno", "Primavera", "Verano", "Otoño" }))
            @RequestParam(required = false) String temporada,

            @Parameter(description = "Fecha desde (dd-MM-yyyy)", example = "01-01-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,

            @Parameter(description = "Fecha hasta (dd-MM-yyyy)", example = "31-12-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta,

            @Parameter(description = "Rentabilidad neta mínima", example = "0")
            @RequestParam(required = false) BigDecimal rentabilidadMinima) {

        return ResponseEntity.ok(rentabilidadApiService.buscarRentabilidadFiltrada(
                festivalId, unidadVentaId, tipoUnidad, temporada, fechaDesde, fechaHasta, rentabilidadMinima));
    }

    @GetMapping("/recaudacion")
    @Operation(
            summary = "Consultar ranking de recaudación filtrado",
            description = "Obtiene el ranking de recaudación por unidad filtrando por festival, unidad, temporada y tipo de unidad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking de recaudación obtenido"),
            @ApiResponse(responseCode = "404", description = "Festival o unidad no encontrados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<List<RankingUnidadDTO>> consultarRecaudacion(
            @Parameter(description = "ID del festival", example = "1")
            @RequestParam(required = false) Long festivalId,

            @Parameter(description = "ID de la unidad de venta", example = "1")
            @RequestParam(required = false) Long unidadVentaId,

            @Parameter(description = "Temporada del festival", schema = @Schema(allowableValues = { "Invierno", "Primavera", "Verano", "Otoño" }))
            @RequestParam(required = false) String temporada,

            @Parameter(description = "Tipo de unidad de venta", schema = @Schema(allowableValues = { "FoodTruck", "PuestoDesarmable" }))
            @RequestParam(required = false) String tipoUnidad) {

        return ResponseEntity.ok(rentabilidadApiService.buscarRecaudacionFiltrada(
                festivalId, unidadVentaId, temporada, tipoUnidad));
    }

    @GetMapping("/dashboard")
    @Operation(
            summary = "Resumen ejecutivo del dashboard administrativo",
            description = "Consolida ingresos, costos y rentabilidad neta total del festival o temporada seleccionada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen consolidado obtenido", content = @Content(schema = @Schema(implementation = DashboardResumenResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existen festivales para los criterios indicados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<DashboardResumenResponseDTO> obtenerResumenDashboard(
            @Parameter(description = "ID del festival seleccionado", example = "1")
            @RequestParam(required = false) Long festivalId,

            @Parameter(description = "Temporada a filtrar si no se especifica festivalId", schema = @Schema(allowableValues = { "Invierno", "Primavera", "Verano", "Otoño" }))
            @RequestParam(required = false) String temporada) {

        return ResponseEntity.ok(rentabilidadApiService.obtenerDashboardResumen(festivalId, temporada));
    }
}
