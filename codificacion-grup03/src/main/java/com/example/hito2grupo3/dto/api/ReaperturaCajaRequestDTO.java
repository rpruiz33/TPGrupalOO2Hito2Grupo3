package com.example.hito2grupo3.dto.api;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para reabrir un cierre de caja existente vía API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para reabrir una caja previamente cerrada")
public class ReaperturaCajaRequestDTO {

    @NotNull(message = "El ID del cajero es obligatorio")
    @Positive(message = "El ID del cajero debe ser positivo")
    @Schema(description = "ID del cajero", example = "4")
    private Long cajeroId;

    @NotNull(message = "El ID de la unidad de venta es obligatorio")
    @Positive(message = "El ID de la unidad de venta debe ser positivo")
    @Schema(description = "ID de la unidad de venta", example = "1")
    private Long unidadVentaId;

    @NotNull(message = "La fecha del cierre a reabrir es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(description = "Fecha del cierre a reabrir (formato dd-MM-yyyy)", example = "25-11-2026")
    private LocalDate fecha;

    @NotBlank(message = "El motivo de reapertura es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    @Schema(description = "Motivo justificado de la reapertura de caja", example = "Faltó registrar pedido de último momento")
    private String motivo;
}

