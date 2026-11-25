package com.example.hito2grupo3.controllers.api;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.dto.ApiErrorDTO;
import com.example.hito2grupo3.dto.api.CierreCajaRequestDTO;
import com.example.hito2grupo3.dto.api.CierreCajaResponseDTO;
import com.example.hito2grupo3.dto.api.ReaperturaCajaRequestDTO;
import com.example.hito2grupo3.dto.api.ResumenCajaResponseDTO;
import com.example.hito2grupo3.services.api.ICierreCajaApiService;

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
 * Hito 3 - API REST de Cierre y Reapertura de Caja (rpruiz33).
 */
@RestController
@RequestMapping(value = "/api/v1/cajas", produces = "application/json")
@Tag(name = "Cierres de Caja", description = "Registro de cierres de turno, reaperturas justificadas y consultas filtradas")
public class CierreCajaRestController {

    private final ICierreCajaApiService cierreCajaApiService;

    public CierreCajaRestController(ICierreCajaApiService cierreCajaApiService) {
        this.cierreCajaApiService = cierreCajaApiService;
    }

    @PostMapping("/cerrar")
    @Operation(
            summary = "Registrar cierre de caja",
            description = "Registra el cierre de caja validando fecha no futura, monto no negativo con hasta 2 decimales "
                    + "y pertenencia del cajero a la unidad de venta (salvo privilegio de administrador).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cierre de caja registrado", content = @Content(schema = @Schema(implementation = CierreCajaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cajero o unidad de venta no encontrados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CierreCajaResponseDTO> registrarCierre(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del cierre de caja",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "cajeroId": 4,
                              "unidadVentaId": 1,
                              "montoRecaudado": 125500.00,
                              "esAdmin": false
                            }
                            """)))
            @Valid @RequestBody CierreCajaRequestDTO request) {
        CierreCajaResponseDTO cierre = cierreCajaApiService.registrarCierre(request);
        return ResponseEntity
                .created(URI.create("/api/v1/cajas/" + cierre.id()))
                .body(cierre);
    }

    @PostMapping("/reabrir")
    @Operation(
            summary = "Reabrir una caja cerrada",
            description = "Cambia el estado de un cierre existente a REABIERTA dejando registrado el motivo y la fecha/hora de reapertura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caja reabierta correctamente", content = @Content(schema = @Schema(implementation = CierreCajaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o cierre no encontrado para esa fecha", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cajero o unidad no encontrados", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CierreCajaResponseDTO> reabrirCaja(
            @Valid @RequestBody ReaperturaCajaRequestDTO request) {
        return ResponseEntity.ok(cierreCajaApiService.reabrirCaja(request));
    }

    @GetMapping
    @Operation(
            summary = "Consultar historial de cierres de caja filtrado",
            description = "Filtra cierres de caja por hasta 6 parámetros: cajeroId, unidadVentaId, estado (CERRADA/REABIERTA), "
                    + "rango de fechas y monto mínimo recaudado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de cierres de caja"),
            @ApiResponse(responseCode = "400", description = "Filtros inválidos", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cajero o unidad indicados no existen", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<List<CierreCajaResponseDTO>> listarCierres(
            @Parameter(description = "ID del cajero", example = "4")
            @RequestParam(required = false) Long cajeroId,

            @Parameter(description = "ID de la unidad de venta", example = "1")
            @RequestParam(required = false) Long unidadVentaId,

            @Parameter(description = "Estado del cierre", schema = @Schema(allowableValues = { "CERRADA", "REABIERTA" }))
            @RequestParam(required = false) String estado,

            @Parameter(description = "Fecha desde (dd-MM-yyyy)", example = "01-01-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,

            @Parameter(description = "Fecha hasta (dd-MM-yyyy)", example = "31-12-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta,

            @Parameter(description = "Monto mínimo recaudado", example = "10000")
            @RequestParam(required = false) BigDecimal montoMinimo) {

        return ResponseEntity.ok(cierreCajaApiService.buscarHistorialFiltrado(
                cajeroId, unidadVentaId, estado, fechaDesde, fechaHasta, montoMinimo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un cierre de caja por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cierre encontrado", content = @Content(schema = @Schema(implementation = CierreCajaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cierre no encontrado", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CierreCajaResponseDTO> obtenerPorId(
            @Parameter(description = "ID del cierre de caja", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(cierreCajaApiService.obtenerPorId(id));
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtener resumen acumulado de caja por email de cajero")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido", content = @Content(schema = @Schema(implementation = ResumenCajaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "El usuario no posee rol de cajero", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<ResumenCajaResponseDTO> obtenerResumenCajero(
            @Parameter(description = "Email del cajero", example = "cajero@epicentro.com", required = true)
            @RequestParam String emailCajero,

            @Parameter(description = "Fecha de referencia (dd-MM-yyyy)", example = "25-11-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fecha) {

        return ResponseEntity.ok(cierreCajaApiService.obtenerResumenPorCajero(emailCajero, fecha));
    }
}

