package com.example.hito2grupo3.controllers.api;

import java.net.URI;
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
import com.example.hito2grupo3.dto.FestivalDTO;
import com.example.hito2grupo3.services.api.IFestivalApiService;

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
 * Hito 3 - API REST de Festivales Gastronómicos (rpruiz33).
 */
@RestController
@RequestMapping(value = "/api/v1/festivales", produces = "application/json")
@Tag(name = "Festivales", description = "Alta y consulta filtrada de festivales y temporadas")
public class FestivalRestController {

    private final IFestivalApiService festivalApiService;

    public FestivalRestController(IFestivalApiService festivalApiService) {
        this.festivalApiService = festivalApiService;
    }

    @PostMapping
    @Operation(
            summary = "Crear un nuevo festival gastronómico",
            description = "Registra un festival validando nombre, rango de fechas (fin al menos 1 día posterior al inicio) "
                    + "y temporada (Invierno, Primavera, Verano, Otoño).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Festival creado correctamente",
                    content = @Content(schema = @Schema(implementation = FestivalDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rango de fechas incorrecto",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "mensaje": "Error de validación",
                                      "detalles": ["La fecha de inicio debe ser anterior o igual a la fecha de finalización"]
                                    }""")))
    })
    public ResponseEntity<FestivalDTO> crearFestival(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del festival a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FestivalDTO.class),
                            examples = @ExampleObject(
                                    name = "Festival de ejemplo",
                                    value = """
                                            {
                                              "nombre": "Epicentro Gourmet Primavera 2026",
                                              "temporada": "Primavera",
                                              "fechaInicio": "2026-11-01",
                                              "fechaFin": "2026-11-15",
                                              "unidadesVentaIds": [1, 2]
                                            }
                                            """)))
            @Valid @RequestBody FestivalDTO request) {
        FestivalDTO creado = festivalApiService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/v1/festivales/" + creado.getId()))
                .body(creado);
    }

    @GetMapping
    @Operation(
            summary = "Listar y filtrar festivales",
            description = "Permite buscar festivales combinando hasta 7 parámetros: nombre parcial, temporada, estado lógico (activo), "
                    + "estado operativo, rango de fechas y unidad de venta participante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de festivales que coinciden con los filtros"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "26-11-2026 11:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "mensaje": "La fecha desde no puede ser posterior a la fecha hasta.",
                                      "detalles": null
                                    }""")))
    })
    public ResponseEntity<List<FestivalDTO>> listarFestivales(
            @Parameter(description = "Búsqueda parcial por nombre del festival", example = "Epicentro")
            @RequestParam(required = false) String nombre,

            @Parameter(description = "Temporada climática", schema = @Schema(allowableValues = { "Invierno", "Primavera", "Verano", "Otoño" }))
            @RequestParam(required = false) String temporada,

            @Parameter(description = "Estado lógico en el sistema (true = activo, false = dado de baja)", example = "true")
            @RequestParam(required = false) Boolean activo,

            @Parameter(description = "Estado operativo calculado", schema = @Schema(allowableValues = { "En curso", "Próximo", "Finalizado", "Dado de baja" }))
            @RequestParam(required = false) String estado,

            @Parameter(description = "Fecha mínima de vigencia (dd-MM-yyyy)", example = "01-01-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,

            @Parameter(description = "Fecha máxima de vigencia (dd-MM-yyyy)", example = "31-12-2026")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta,

            @Parameter(description = "ID de una unidad de venta habilitada en el festival", example = "1")
            @RequestParam(required = false) Long unidadVentaId) {

        return ResponseEntity.ok(festivalApiService.buscarFiltrados(
                nombre, temporada, activo, estado, fechaDesde, fechaHasta, unidadVentaId));
    }
}
