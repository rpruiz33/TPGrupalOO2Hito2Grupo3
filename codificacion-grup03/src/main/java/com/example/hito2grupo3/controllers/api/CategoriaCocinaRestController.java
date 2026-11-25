package com.example.hito2grupo3.controllers.api;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.dto.ApiErrorDTO;
import com.example.hito2grupo3.dto.CategoriaCocinaDTO;
import com.example.hito2grupo3.services.api.ICategoriaCocinaApiService;

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
 * Hito 3 - API REST de Categorías de Cocina (rpruiz33).
 */
@RestController
@RequestMapping(value = "/api/v1/categorias-cocina", produces = "application/json")
@Tag(name = "Categorías de Cocina", description = "ABM de categorías culinarias, plus salarial fijo y asignación de cocineros")
public class CategoriaCocinaRestController {

    private final ICategoriaCocinaApiService categoriaApiService;

    public CategoriaCocinaRestController(ICategoriaCocinaApiService categoriaApiService) {
        this.categoriaApiService = categoriaApiService;
    }

    @PostMapping
    @Operation(
            summary = "Crear una categoría de cocina",
            description = "Da de alta una nueva categoría culinaria con su plus fijo aplicado en las liquidaciones de cocineros.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada", content = @Content(schema = @Schema(implementation = CategoriaCocinaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría duplicada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> crearCategoria(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la categoría de cocina",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "nombre": "Maestro Parrillero",
                              "plusFijo": 180000.00
                            }
                            """)))
            @Valid @RequestBody CategoriaCocinaDTO request) {
        CategoriaCocinaDTO creada = categoriaApiService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/v1/categorias-cocina/" + creada.getId()))
                .body(creada);
    }

    @GetMapping
    @Operation(
            summary = "Listar y filtrar categorías de cocina",
            description = "Filtra categorías por 4 parámetros combinables: nombre parcial, estado activo y rango de plus salarial.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de categorías encontradas"),
            @ApiResponse(responseCode = "400", description = "Rango de plus inválido", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<List<CategoriaCocinaDTO>> listarCategorias(
            @Parameter(description = "Búsqueda parcial por nombre", example = "Cocinero")
            @RequestParam(required = false) String nombre,

            @Parameter(description = "Estado de actividad", example = "true")
            @RequestParam(required = false) Boolean activa,

            @Parameter(description = "Plus salarial mínimo", example = "50000")
            @RequestParam(required = false) BigDecimal plusMinimo,

            @Parameter(description = "Plus salarial máximo", example = "200000")
            @RequestParam(required = false) BigDecimal plusMaximo) {

        return ResponseEntity.ok(categoriaApiService.buscarFiltradas(nombre, activa, plusMinimo, plusMaximo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría de cocina por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = CategoriaCocinaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> obtenerPorId(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoriaApiService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría de cocina")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> actualizarCategoria(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoriaCocinaDTO request) {
        return ResponseEntity.ok(categoriaApiService.actualizar(id, request));
    }

    @PatchMapping("/{id}/baja")
    @Operation(summary = "Dar de baja lógica una categoría de cocina")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría dada de baja"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> darDeBaja(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoriaApiService.darDeBaja(id));
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar una categoría de cocina dada de baja")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría reactivada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> reactivar(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoriaApiService.reactivar(id));
    }

    @PostMapping("/{id}/cocineros/{cocineroId}")
    @Operation(summary = "Asignar un cocinero a una categoría de cocina")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cocinero asignado a la categoría"),
            @ApiResponse(responseCode = "404", description = "Categoría o cocinero no encontrado", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<CategoriaCocinaDTO> asignarCocinero(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del cocinero (Staff)", example = "6") @PathVariable Long cocineroId) {
        return ResponseEntity.ok(categoriaApiService.asignarCocinero(id, cocineroId));
    }
}
