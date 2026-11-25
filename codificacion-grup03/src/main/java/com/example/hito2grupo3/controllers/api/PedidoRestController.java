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
import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PedidoResponseDTO;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.services.IPedidoService;

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
 * Hito 3 - API REST de pedidos.
 *
 * POST /api/v1/pedidos -> alta de un pedido con sus platos.
 * GET /api/v1/pedidos -> historial de pedidos filtrado por festival,
 * unidad de venta y plato (+ rango de fechas y estado).
 */
@RestController
@RequestMapping(value = "/api/v1/pedidos", produces = "application/json")
@Tag(name = "Pedidos", description = "Alta e historial de pedidos de las unidades de venta")
public class PedidoRestController {

        // Servicio que aplica las reglas de negocio de pedidos y realiza las consultas.
        private final IPedidoService pedidoService;

        // Inyección del servicio REST utilizado por todos los endpoints de pedidos.
        public PedidoRestController(IPedidoService pedidoService) {
                this.pedidoService = pedidoService;
        }

        // ==========================================
        // POST - ALTA DE PEDIDO
        // ==========================================

        // POST: valida el pedido y delega su registro al servicio de dominio.
        @PostMapping
        @Operation(summary = "Registrar un pedido", description = "Da de alta un pedido con sus platos y cantidades. Aplica las mismas reglas "
                        + "que el alta desde la web: la fecha (formato dd-MM-yyyy) debe ser la actual, el festival debe estar "
                        + "vigente, la unidad habilitada en ese festival, los platos activos de esa unidad, "
                        + "y el empleado (staffId) debe ser RESPONSABLE o CAJERO asignado a la unidad y activo. "
                        + "El precio unitario se toma del plato al momento de la venta.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Pedido registrado", content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
        })
        public ResponseEntity<PedidoResponseDTO> registrarPedido(
                        @Parameter(description = "Id del empleado (Staff) que registra el pedido", example = "2", required = true) @RequestParam Long staffId,

                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del pedido", required = true, content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "fecha": "21-11-2026",
                                          "festivalId": 1,
                                          "unidadVentaId": 1,
                                          "items": [
                                            { "platoId": 1, "cantidad": 2 },
                                            { "platoId": 2, "cantidad": 1 }
                                          ]
                                        }
                                        """))) @Valid @RequestBody PedidoAltaDTO datos) {

                Pedido pedido = pedidoService.registrarPedido(datos, staffId);
                PedidoResponseDTO respuesta = PedidoResponseDTO.desde(pedido);

                return ResponseEntity
                                .created(URI.create("/api/v1/pedidos/" + pedido.getId()))
                                .body(respuesta);
        }

        // ==========================================
        // GET - HISTORIAL DE PEDIDOS CON FILTROS
        // ==========================================

        // GET: devuelve el historial general aplicando los filtros recibidos.
        @GetMapping
        @Operation(summary = "Historial de pedidos filtrado", description = "Devuelve los pedidos ordenados del más reciente al más antiguo. "
                        + "Filtros principales: festival, unidad de venta y plato. "
                        + "Opcionales: rango de fechas de la transacción y estado. "
                        + "Todos los filtros se pueden combinar; si no se envía ninguno se devuelven todos.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de pedidos (puede estar vacío)"),
                        @ApiResponse(responseCode = "400", description = "Filtros inválidos (fechas invertidas, estado incorrecto, formato)", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class), examples = @ExampleObject(value = """
                                        {
                                          "timestamp": "30-12-2026 12:30",
                                          "status": 400,
                                          "error": "Bad Request",
                                          "mensaje": "La fecha desde no puede ser posterior a la fecha hasta.",
                                          "detalles": null
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "El festival, la unidad o el plato indicados no existen", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class), examples = @ExampleObject(value = """
                                        {
                                          "timestamp": "20-11-2026 12:30",
                                          "status": 404,
                                          "error": "Not Found",
                                          "mensaje": "No existe el festival con id 99.",
                                          "detalles": null
                                        }
                                        """)))
        })
        public ResponseEntity<List<PedidoResponseDTO>> obtenerHistorial(
                        @Parameter(description = "Id del festival", example = "1") @RequestParam(required = false) Long festivalId,

                        @Parameter(description = "Id de la unidad de venta", example = "1") @RequestParam(required = false) Long unidadVentaId,

                        @Parameter(description = "Id de un plato: trae los pedidos que lo incluyen", example = "1") @RequestParam(required = false) Long platoId,

                        @Parameter(description = "Fecha de transacción desde (dd-MM-yyyy)", example = "01-11-2026") @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,
                        @Parameter(description = "Fecha de transacción hasta (dd-MM-yyyy)", example = "30-11-2026") @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta,

                        @Parameter(description = "Estado: A = activo, R = anulado", schema = @Schema(allowableValues = {
                                        "A", "R" })) @RequestParam(required = false) String estado) {

                List<PedidoResponseDTO> pedidos = pedidoService.buscarHistorialFiltrado(
                                festivalId, unidadVentaId, platoId, fechaDesde, fechaHasta, estado);

                return ResponseEntity.ok(pedidos);
        }
        // ==========================================
        // GET - HISTORIAL DEL EMPLEADO (Misma logica web)
        // ==========================================

        // GET: aplica los permisos y filtros correspondientes al historial del empleado.
        @GetMapping("/historial")
        @Operation(summary = "Historial de pedidos del empleado", description = "Funciona igual que la página de historial: "
                        + "el empleado (staffId) debe existir, estar activo y ser RESPONSABLE o CAJERO asociado a una unidad de venta. "
                        + "Solo devuelve pedidos de sus unidades; el RESPONSABLE ve todos los festivales y el CAJERO "
                        + "solo los festivales vigentes. Filtros opcionales: N.º de pedido, unidad, festival, "
                        + "empleado que lo registró, estado y rango de fecha de registro. "
                        + "Ordenado del más reciente al más antiguo.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de pedidos del empleado (puede estar vacío)"),
                        @ApiResponse(responseCode = "400", description = "Falta staffId o filtros inválidos", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))),
                        @ApiResponse(responseCode = "403", description = "Empleado inactivo, sin rol RESPONSABLE/CAJERO, sin unidad asociada o sin acceso a la unidad filtrada", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class), examples = @ExampleObject(value = """
                                        {
                                          "timestamp": "21-11-2026 12:30",
                                          "status": 403,
                                          "error": "Forbidden",
                                          "mensaje": "No tenés acceso a esta unidad de venta.",
                                          "detalles": null
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "El empleado, la unidad o el festival no existen", content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
        })
        public ResponseEntity<List<PedidoResponseDTO>> obtenerHistorialDelEmpleado(
                        @Parameter(description = "Id del empleado (Staff) que consulta", example = "2", required = true) @RequestParam Long staffId,
                        @Parameter(description = "N.º de pedido", example = "15") @RequestParam(required = false) Long pedidoId,
                        @Parameter(description = "Id de la unidad de venta (debe ser una de las del empleado)", example = "1") @RequestParam(required = false) Long unidadVentaId,
                        @Parameter(description = "Id del festival", example = "1") @RequestParam(required = false) Long festivalId,
                        @Parameter(description = "Id del empleado que registró el pedido (operador)", example = "2") @RequestParam(required = false) Long empleadoId,
                        @Parameter(description = "Estado: A = activo, R = anulado", schema = @Schema(allowableValues = { "A", "R" })) @RequestParam(required = false) String estado,
                        @Parameter(description = "Fecha de registro desde (dd-MM-yyyy)", example = "01-11-2026") @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaDesde,
                        @Parameter(description = "Fecha de registro hasta (dd-MM-yyyy)", example = "15-12-2026") @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaHasta) {

                List<PedidoResponseDTO> pedidos = pedidoService.buscarHistorialDelStaff(
                                staffId, pedidoId, unidadVentaId, festivalId, empleadoId, estado, fechaDesde, fechaHasta);

                return ResponseEntity.ok(pedidos);
        }
}
