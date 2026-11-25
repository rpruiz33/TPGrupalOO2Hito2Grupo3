package com.example.hito2grupo3.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para registrar un cierre de caja vía API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para registrar un cierre de caja")
public class CierreCajaRequestDTO {

    @NotNull(message = "El ID del cajero es obligatorio")
    @Positive(message = "El ID del cajero debe ser positivo")
    @Schema(description = "ID del cajero (Staff) que realiza el cierre", example = "4")
    private Long cajeroId;

    @NotNull(message = "El ID de la unidad de venta es obligatorio")
    @Positive(message = "El ID de la unidad de venta debe ser positivo")
    @Schema(description = "ID de la unidad de venta", example = "1")
    private Long unidadVentaId;

    @PastOrPresent(message = "La fecha de cierre no puede ser futura")
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(description = "Fecha del cierre (formato dd-MM-yyyy; opcional, por defecto hoy)", example = "25-11-2026")
    private LocalDate fecha;

    @NotNull(message = "El monto recaudado es obligatorio")
    @PositiveOrZero(message = "El monto recaudado debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El monto solo puede tener hasta 2 decimales")
    @Schema(description = "Monto total recaudado en el turno", example = "125500.00")
    private BigDecimal montoRecaudado;

    @Schema(description = "Indica si la operación es ejecutada con privilegios de administrador", example = "false")
    private boolean esAdmin = false;
}

