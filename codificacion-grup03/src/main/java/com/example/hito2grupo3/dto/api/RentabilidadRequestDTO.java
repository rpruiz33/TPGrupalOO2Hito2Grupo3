package com.example.hito2grupo3.dto.api;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para solicitar el cálculo de rentabilidad neta vía API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parámetros para calcular la rentabilidad neta de una unidad en un festival")
public class RentabilidadRequestDTO {

    @NotNull(message = "El ID del festival es obligatorio")
    @Positive(message = "El ID del festival debe ser positivo")
    @Schema(description = "ID del festival a analizar", example = "1")
    private Long festivalId;

    @NotNull(message = "El ID de la unidad de venta es obligatorio")
    @Positive(message = "El ID de la unidad de venta debe ser positivo")
    @Schema(description = "ID de la unidad de venta a analizar", example = "1")
    private Long unidadVentaId;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(description = "Fecha desde (opcional, por defecto fecha de inicio del festival)", example = "01-11-2026")
    private LocalDate fechaDesde;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(description = "Fecha hasta (opcional, por defecto fecha de fin del festival)", example = "15-11-2026")
    private LocalDate fechaHasta;

    @AssertTrue(message = "La fecha desde no puede ser posterior a la fecha hasta")
    @Schema(hidden = true)
    public boolean isRangoValido() {
        if (fechaDesde == null || fechaHasta == null) {
            return true;
        }
        return !fechaDesde.isAfter(fechaHasta);
    }
}

