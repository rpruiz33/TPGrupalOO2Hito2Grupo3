package com.example.hito2grupo3.controllers.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.dto.ApiErrorDTO;
import com.example.hito2grupo3.dto.LiquidacionDTO;
import com.example.hito2grupo3.dto.api.LiquidacionRequestDTO;
import com.example.hito2grupo3.services.ILiquidacionService;
import com.example.hito2grupo3.services.api.ILiquidacionApiService;

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
 * Hito 3 - API REST de Liquidación de Haberes (rpruiz33).
 */
@RestController
@RequestMapping(value = "/api/v1/liquidaciones", produces = "application/json")
@Tag(name = "Liquidaciones", description = "Cálculo, registro y consulta filtrada de liquidaciones de haberes")
public class LiquidacionRestController {

    private final ILiquidacionApiService liquidacionApiService;

    public LiquidacionRestController(ILiquidacionApiService liquidacionApiService) {
        this.liquidacionApiService = liquidacionApiService;
    }

    @PostMapping
    @Operation(
            summary = "Calcular y registrar liquidaciones de haberes",
            description = "Calcula y persiste las liquidaciones del personal para un festival. "
                    + "Se puede filtrar por unidad de venta, código comercial o email exacto del empleado. "
                    + "Aplica sueldo base mínimo de SueldoProperties, proporcionalidad quincenal, antigüedad o plus por categoría.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Liquidaciones calculadas y registradas",
                    content = @Content(schema = @Schema(implementation = ILiquidacionService.ResultadoLiquidacion.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos o regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "mensaje": "Error de validación",
                                      "detalles": ["El ID del festival es obligatorio"]
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Festival o unidad no encontrados",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "mensaje": "No existe el festival con id 99.",
                                      "detalles": null
                                    }""")))
    })
    public ResponseEntity<ILiquidacionService.ResultadoLiquidacion> registrarLiquidaciones(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Criterios de liquidación",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LiquidacionRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Liquidación por festival y unidad",
                                    value = """
                                            {
                                              "festivalId": 1,
                                              "unidadVentaId": 1
                                            }
                                            """)))
            @Valid @RequestBody LiquidacionRequestDTO request) {
        ILiquidacionService.ResultadoLiquidacion resumen = liquidacionApiService.registrarLiquidaciones(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }

    @GetMapping
    @Operation(
            summary = "Consultar historial de liquidaciones filtrado",
            description = "Permite filtrar las liquidaciones persistidas combinando hasta 8 parámetros: "
                    + "festival, unidad de venta, email, rol, rango de fechas y rango de montos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de liquidaciones encontradas"),
            @ApiResponse(responseCode = "400", description = "Filtros de fecha o monto inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "mensaje": "La fecha desde no puede ser posterior a la fecha hasta.",
                                      "detalles": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Festival o unidad indicados no existen",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "mensaje": "No existe el festival con id 99.",
                                      "detalles": null
                                    }""")))
    })
    public ResponseEntity<List<LiquidacionDTO>> buscarHistorial(
            @Parameter(description = "ID del festival", example = "1")
            @RequestParam(required = false) Long festivalId,

            @Parameter(description = "ID de la unidad de venta", example = "1")
            @RequestParam(required = false) Long unidadVentaId,

            @Parameter(description = "Búsqueda parcial por email del empleado", example = "cajero@epicentro.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Rol operativo", schema = @Schema(allowableValues = { "Cocinero", "Cajero", "Responsable", "Empleado" }))
            @RequestParam(required = false) String rol,

            @Parameter(description = "Fecha mínima del período liquidado (dd-MM-yyyy)", example = "01-01-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,

            @Parameter(description = "Fecha máxima del período liquidado (dd-MM-yyyy)", example = "31-12-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta,

            @Parameter(description = "Monto neto mínimo liquidado", example = "50000")
            @RequestParam(required = false) BigDecimal montoMinimo,

            @Parameter(description = "Monto neto máximo liquidado", example = "500000")
            @RequestParam(required = false) BigDecimal montoMaximo) {

        return ResponseEntity.ok(liquidacionApiService.buscarHistorialFiltrado(
                festivalId, unidadVentaId, email, rol, fechaDesde, fechaHasta, montoMinimo, montoMaximo));
    }
}
