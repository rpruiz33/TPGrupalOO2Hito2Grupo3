package com.example.hito2grupo3.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para ejecutar o simular una liquidación de haberes vía API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parámetros para calcular o registrar liquidaciones de haberes")
public class LiquidacionRequestDTO {

    // Festival obligatorio sobre el cual se calcularán las liquidaciones.
    @NotNull(message = "El ID del festival es obligatorio")
    @Positive(message = "El ID del festival debe ser positivo")
    @Schema(description = "ID del festival a liquidar", example = "1")
    private Long festivalId;

    // Unidad opcional para limitar el cálculo a una sola unidad de venta.
    @Positive(message = "El ID de la unidad de venta debe ser positivo")
    @Schema(description = "ID de la unidad de venta (opcional si se liquida todo el festival o por email)", example = "1")
    private Long unidadVentaId;

    // Email opcional para calcular la liquidación de un empleado específico.
    @Email(message = "El email del empleado debe tener un formato válido")
    @Schema(description = "Email exacto del empleado a liquidar (opcional)", example = "cajero@epicentro.com")
    private String emailEmpleado;

    // Código comercial opcional como alternativa para identificar la unidad.
    @Schema(description = "Código comercial de la unidad de venta (opcional)", example = "FT-0000001")
    private String codigoUnidad;
}

