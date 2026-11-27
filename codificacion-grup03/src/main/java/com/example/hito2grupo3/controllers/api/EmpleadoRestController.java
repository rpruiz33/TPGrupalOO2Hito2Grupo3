
package com.example.hito2grupo3.controllers.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.services.IEmpleadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/empleados")
@Tag(name = "Empleados", description = "Gestión y consulta de empleados")
public class EmpleadoRestController {

    private final IEmpleadoService empleadoService;

    public EmpleadoRestController(IEmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    // ==========================================
    // GET - BUSCAR EMPLEADOS CON FILTROS
    // ==========================================

    @GetMapping
    @Operation(
        summary = "Buscar empleados por filtros",
        description = "Permite buscar empleados de una unidad de venta filtrando por nombre, apellido, DNI, estado, rol y email."
    )
    public ResponseEntity<List<EmpleadoDTO>> buscarEmpleados(

        @Parameter(description = "ID de la unidad de venta", example = "1")
        @RequestParam Long unidadId,

        @Parameter(description = "Nombre del empleado", example = "Juan")
        @RequestParam(required = false) String nombre,

        @Parameter(description = "Apellido del empleado", example = "Perez")
        @RequestParam(required = false) String apellido,

        @Parameter(description = "DNI del empleado", example = "12345678")
        @RequestParam(required = false) String dni,

        @Parameter(description = "Estado: ACTIVOS, INACTIVOS o TODOS", example = "TODOS")
        @RequestParam(defaultValue = "TODOS") String estado,

        @Parameter(description = "Rol del empleado", example = "CAJERO")
        @RequestParam(required = false) String rol,

        @Parameter(description = "Correo electrónico", example = "gmail.com")
        @RequestParam(required = false) String email
    ) {

        List<Staff> empleados = empleadoService.filtrarEmpleadosPorUnidad(
            unidadId,
            nombre,
            apellido,
            dni,
            estado,
            rol,
            email
        );

        List<EmpleadoDTO> resultado = empleados.stream()
            .map(empleado ->
                empleadoService.obtenerDtoPorId(empleado.getId())
            )
            .toList();

        return ResponseEntity.ok(resultado);
    }


    // ==========================================
    // POST - ALTA DE EMPLEADO
    // ==========================================

    @PostMapping
    @Operation(
        summary = "Dar de alta un empleado",
        description = "Registra un nuevo empleado, crea su usuario y envía sus credenciales iniciales."
    )
    public ResponseEntity<EmpleadoDTO> crearEmpleado(

        @Valid @RequestBody EmpleadoDTO dto,

        Authentication authentication

    ) {

        // Crear el empleado utilizando el servicio existente
        Staff nuevoStaff = empleadoService.crearEmpleado(dto);

        // Asociar el empleado a la unidad del responsable
        if (authentication != null
                && authentication.isAuthenticated()
                && nuevoStaff != null) {

            empleadoService.asociarEmpleadoAUnidadDelCreador(
                authentication.getName(),
                nuevoStaff
            );
        }

        // Obtener el DTO del empleado creado
        EmpleadoDTO respuesta =
            empleadoService.obtenerDtoPorId(nuevoStaff.getId());

        // Devolver respuesta HTTP 201 Created
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(respuesta);
    }

}
